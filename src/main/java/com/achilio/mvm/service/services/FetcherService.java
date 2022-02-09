package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.SCOPE_CACHED;
import static com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.SCOPE_IN;
import static com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.SCOPE_OUT;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedMaterializedViewEvent;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class FetcherService {

  BigQueryMaterializedViewStatementBuilder statementBuilder;

  @PersistenceContext
  private EntityManager entityManager;

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
    return root.getProcessedBytes();
  }

  public List<FetchedProject> fetchAllProjects() throws Exception {
    return fetcher().fetchAllProjects();
  }

  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    return fetcher(projectId).fetchProject(projectId);
  }

  public List<FetchedDataset> fetchAllDatasets(String projectId) throws Exception {
    return fetcher(projectId).fetchAllDatasets();
  }

  public FetchedDataset fetchDataset(String projectId, String datasetName) {
    return fetcher(projectId).fetchDataset(datasetName);
  }

  public List<FetchedQuery> fetchQueries(String projectId) {
    return fetcher(projectId).fetchAllQueries();
  }

  public List<FetchedQuery> fetchQueriesSince(String projectId, int lastDays) {
    return fetchQueriesSince(projectId, daysToMillis(lastDays));
  }

  public List<FetchedQuery> fetchQueriesSince(String projectId, long fromTimestamp) {
    return fetcher(projectId).fetchAllQueriesFrom(fromTimestamp);
  }

  public FetchedTable fetchTable(String projectId, String datasetName, String tableName)
      throws Exception {
    return fetcher(projectId).fetchTable(datasetName, tableName);
  }

  public Set<FetchedTable> fetchAllTables(String projectId) {
    return fetcher(projectId).fetchAllTables();
  }

  public Set<FetchedTable> fetchTableNamesInDataset(String projectId, String datasetName) {
    return fetcher(projectId).fetchTableNamesInDataset(datasetName);
  }

  public GlobalQueryStatistics getStatistics(String projectId, int lastDays) throws Exception {
    return getStatistics(projectId, lastDays, false);
  }

  public List<FetchedMaterializedViewEvent> getMaterializedViewEvents(String projectId,
      int lastDays) {
    return fetcher(projectId).fetchMaterializedViewEvents(daysToMillis(lastDays));
  }

  public GlobalQueryStatistics getStatistics(
      String projectId, int lastDays, boolean enableIneligibilityStats) throws Exception {
    return getStatistics(fetchQueriesSince(projectId, lastDays), enableIneligibilityStats);
  }

  public Map<String, Long> getDailyStatistics(String projectId, int lastDays) {
    return fetchQueriesSince(projectId, lastDays)
        .stream()
        .collect(
            Collectors.groupingBy(q -> q.getDate().with(TemporalAdjusters.ofDateAdjuster(d -> d)),
                Collectors.mapping(FetchedQuery::getStatistics, Collectors.toList())))
        .entrySet()
        .stream()
        .collect(
            Collectors.toMap(e -> e.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                e -> averageQueryCost(e.getValue())));
    // Convert in Array of String Array
        /*.map(entry -> Arrays.asList(entry.getKey(),
            String.valueOf(entry.getValue().getProcessedBytes()),
            String.valueOf(entry.getValue().getBilledBytes())))
        .collect(Collectors.toList());*/

  }

  public GlobalQueryStatistics getStatistics(
      List<FetchedQuery> queries, boolean enableIneligibilityStats) {
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
    GlobalQueryStatistics global = new GlobalQueryStatistics(enableIneligibilityStats);
    global.addStatistic(SCOPE_IN, new QueryStatistics(selectIn, enableIneligibilityStats));
    global.addStatistic(SCOPE_OUT, new QueryStatistics(selectOut, enableIneligibilityStats));
    global.addStatistic(SCOPE_CACHED, new QueryStatistics(selectCached, enableIneligibilityStats));
    return global;
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

  private long daysToMillis(int days) {
    return System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
  }
}
