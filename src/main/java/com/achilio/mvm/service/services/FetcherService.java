package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedOrganization;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** All the useful services to generate relevant Materialized Views. */
@Service
public class FetcherService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  BigQueryMaterializedViewStatementBuilder statementBuilder;
  @Autowired ConnectionService connectionService;

  @Value("${application.name}")
  private String applicationName;

  public FetcherService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public static long averageQueryCost(List<QueryUsageStatistics> statistics) {
    QueryUsageStatistics root = new QueryUsageStatistics(0, 0, 0);
    statistics.forEach(root::addQueryUsageStatistics);
    return root.getQueryCount() == 0 ? 0 : root.getProcessedBytes() / root.getQueryCount();
  }

  public List<FetchedProject> fetchAllProjects() {
    DatabaseFetcher fetcher = fetcher();
    List<FetchedProject> projectList = fetcher.fetchAllProjects();
    fetcher.close();
    return projectList;
  }

  public List<FetchedOrganization> fetchAllOrganizations() {
    DatabaseFetcher fetcher = fetcher();
    List<FetchedOrganization> organizationList = fetcher.fetchAllOrganizations();
    fetcher.close();
    return organizationList;
  }

  public List<FetchedProject> fetchAllProjectsFromOrg(AOrganization organization) {
    DatabaseFetcher fetcher = fetcher();
    List<FetchedProject> projectList = fetcher.fetchAllProjectsFromOrg(organization);
    fetcher.close();
    return projectList;
  }

  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    DatabaseFetcher fetcher = fetcher();
    FetchedProject fetchedProject = fetcher.fetchProject(projectId);
    fetcher.close();
    return fetchedProject;
  }

  public List<FetchedDataset> fetchAllDatasets(String projectId) {
    DatabaseFetcher fetcher = fetcher(projectId);
    List<FetchedDataset> datasetList = fetcher.fetchAllDatasets(projectId);
    fetcher.close();
    return datasetList;
  }

  public FetchedDataset fetchDataset(String projectId, String datasetName) {
    DatabaseFetcher fetcher = fetcher(projectId);
    FetchedDataset fetchedDataset = fetcher.fetchDataset(datasetName);
    fetcher.close();
    return fetchedDataset;
  }

  public List<FetchedQuery> fetchQueriesSinceLastDays(String projectId, int lastDays) {
    return fetchQueriesSinceTimestamp(projectId, daysToMillis(lastDays));
  }

  public List<FetchedQuery> fetchQueriesSinceTimestamp(String projectId, long fromTimestamp) {
    DatabaseFetcher fetcher = fetcher(projectId);
    List<FetchedQuery> queryList = fetcher.fetchAllQueriesFrom(fromTimestamp);
    fetcher.close();
    return queryList;
  }

  public Set<FetchedTable> fetchAllTables(String projectId) {
    DatabaseFetcher fetcher = fetcher(projectId);
    Set<FetchedTable> fetchedTableSet = fetcher.fetchAllTables();
    fetcher.close();
    return fetchedTableSet;
  }

  public GlobalQueryStatistics getStatistics(String projectId, int lastDays) throws Exception {
    return getStatistics(fetchQueriesSinceLastDays(projectId, lastDays));
  }

  public GlobalQueryStatistics getStatistics(List<FetchedQuery> queries) {
    // Select using materialized view
    List<FetchedQuery> selectIn =
        queries.stream().filter(FetchedQuery::isUsingMaterializedView).collect(Collectors.toList());
    // Select using cache
    List<FetchedQuery> selectCached =
        queries.stream().filter(FetchedQuery::isUsingCache).collect(Collectors.toList());
    // Select using table source
    List<FetchedQuery> selectOut =
        queries.stream()
            .filter(q -> !q.isUsingMaterializedView() && !q.isUsingCache())
            .collect(Collectors.toList());
    GlobalQueryStatistics global = new GlobalQueryStatistics();
    global.addStatistic(Scope.IN, new QueryStatistics(selectIn));
    global.addStatistic(Scope.OUT, new QueryStatistics(selectOut));
    global.addStatistic(Scope.CACHED, new QueryStatistics(selectCached));
    return global;
  }

  public Userinfo getUserInfo() {
    try {
      Oauth2 oauth2 =
          new Oauth2.Builder(
                  HTTP_TRANSPORT,
                  JSON_FACTORY,
                  new GoogleCredential().setAccessToken(getAccessToken()))
              .setApplicationName(applicationName)
              .build();
      return oauth2.userinfo().get().execute();
    } catch (Exception e) {
      throw new RuntimeException("Error while retrieve user info");
    }
  }

  private DatabaseFetcher fetcher() {
    return fetcher(null);
  }

  private DatabaseFetcher fetcher(String projectId) throws ProjectNotFoundException {
    ServiceAccountConnection serviceAccountConnection =
        (ServiceAccountConnection) connectionService.getAllConnections(getContextTeamName()).get(0);
    String serviceAccount = serviceAccountConnection.getContent();
    return new BigQueryDatabaseFetcher(serviceAccount, projectId);
  }

  public String getAccessToken() {
    return ((SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication())
        .getCredentials()
        .getAccessToken()
        .getTokenValue();
  }

  private long daysToMillis(int days) {
    return System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
  }

  public List<String> fetchMissingPermissions(String projectId) {
    return fetcher().fetchMissingPermissions(projectId);
  }
}
