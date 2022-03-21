package com.achilio.mvm.service.services;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** All the useful services to generate relevant Materialized Views. */
@Service
public class FetcherService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  BigQueryMaterializedViewStatementBuilder statementBuilder;

  @Value("${application.name}")
  private String applicationName;

  public FetcherService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public static QueryUsageStatistics merge(List<QueryUsageStatistics> statistics) {
    QueryUsageStatistics root = new QueryUsageStatistics(0, 0, 0);
    statistics.forEach(root::addQueryUsageStatistics);
    return root;
  }

  public static long averageQueryCost(List<QueryUsageStatistics> statistics) {
    QueryUsageStatistics root = new QueryUsageStatistics(0, 0, 0);
    statistics.forEach(root::addQueryUsageStatistics);
    return root.getQueryCount() == 0 ? 0 : root.getProcessedBytes() / root.getQueryCount();
  }

  public List<FetchedProject> fetchAllProjects() {
    return fetcher().fetchAllProjects();
  }

  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    return fetcher(projectId).fetchProject(projectId);
  }

  public List<FetchedDataset> fetchAllDatasets(String projectId) {
    return fetcher(projectId).fetchAllDatasets(projectId);
  }

  public FetchedDataset fetchDataset(String projectId, String datasetName) {
    return fetcher(projectId).fetchDataset(datasetName);
  }

  public List<FetchedQuery> fetchQueriesSinceLastDays(String projectId, int lastDays) {
    return fetchQueriesSinceTimestamp(projectId, daysToMillis(lastDays));
  }

  public List<FetchedQuery> fetchQueriesSinceTimestamp(String projectId, long fromTimestamp) {
    return fetcher(projectId).fetchAllQueriesFrom(fromTimestamp);
  }

  public Set<FetchedTable> fetchAllTables(String projectId) {
    return fetcher(projectId).fetchAllTables();
  }

  public GlobalQueryStatistics getStatistics(String projectId, int lastDays) throws Exception {
    return getStatistics(fetchQueriesSinceLastDays(projectId, lastDays));
  }

  public List<StatEntry> getDailyStatistics(String projectId, int lastDays) {
    Map<LocalDate, List<QueryUsageStatistics>> fetched =
        fetchQueriesSinceLastDays(projectId, lastDays).parallelStream()
            .collect(
                Collectors.groupingBy(
                    q -> q.getDate().with(TemporalAdjusters.ofDateAdjuster(d -> d)),
                    Collectors.mapping(FetchedQuery::getStatistics, Collectors.toList())));
    for (int i = 0; i < lastDays; i++) {
      LocalDate currentDay =
          LocalDate.now().minusDays(i).with(TemporalAdjusters.ofDateAdjuster(d -> d));
      if (!fetched.containsKey(currentDay)) {
        fetched.put(currentDay, Collections.emptyList());
      }
    }
    return fetched.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> e.getKey().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
                e -> averageQueryCost(e.getValue())))
        .entrySet()
        .stream()
        .map(e -> new StatEntry(e.getKey(), e.getValue()))
        .sorted(Comparator.comparingLong(StatEntry::getTimestamp))
        .collect(Collectors.toList());
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
    SimpleGoogleCredentialsAuthentication authentication =
        (SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication();
    return new BigQueryDatabaseFetcher(authentication.getCredentials(), projectId);
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

  public static class StatEntry {

    private final long timestamp;
    private final long value;

    public StatEntry(long timestamp, long value) {
      this.timestamp = timestamp;
      this.value = value;
    }

    public long getTimestamp() {
      return this.timestamp;
    }

    public long getValue() {
      return this.value;
    }
  }
}
