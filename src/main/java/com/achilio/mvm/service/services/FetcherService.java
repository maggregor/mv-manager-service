package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** All the useful services to generate relevant Materialized Views. */
@Service
public class FetcherService {

  BigQueryMaterializedViewStatementBuilder statementBuilder;
  @Autowired ConnectionService connectionService;

  public FetcherService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public FetchedProject fetchProject(String projectId, Connection connection)
      throws ProjectNotFoundException {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    FetchedProject fetchedProject = fetcher.fetchProject(projectId);
    fetcher.close();
    return fetchedProject;
  }

  public List<FetchedDataset> fetchAllDatasets(String projectId, Connection connection) {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    List<FetchedDataset> datasetList = fetcher.fetchAllDatasets(projectId);
    fetcher.close();
    return datasetList;
  }

  public FetchedDataset fetchDataset(String projectId, String datasetName, Connection connection) {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    FetchedDataset fetchedDataset = fetcher.fetchDataset(datasetName);
    fetcher.close();
    return fetchedDataset;
  }

  public List<FetchedQuery> fetchQueriesSinceLastDays(
      String projectId, Connection connection, int lastDays) {
    return fetchQueriesSinceTimestamp(projectId, connection, daysToMillis(lastDays));
  }

  public List<FetchedQuery> fetchQueriesSinceTimestamp(
      String projectId, Connection connection, long fromTimestamp) {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    List<FetchedQuery> queryList = fetcher.fetchAllQueriesFrom(fromTimestamp);
    fetcher.close();
    return queryList;
  }

  public Set<FetchedTable> fetchAllTables(String projectId, Connection connection) {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    Set<FetchedTable> fetchedTableSet = fetcher.fetchAllTables();
    fetcher.close();
    return fetchedTableSet;
  }

  public GlobalQueryStatistics getStatistics(String projectId, Connection connection, int lastDays)
      throws Exception {
    return getStatistics(fetchQueriesSinceLastDays(projectId, connection, lastDays));
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

  private DatabaseFetcher fetcher(String projectId, Connection connection)
      throws ProjectNotFoundException {
    ServiceAccountConnection saConnection = (ServiceAccountConnection) connection;
    String serviceAccount = saConnection.getServiceAccountKey();
    return new BigQueryDatabaseFetcher(serviceAccount, projectId);
  }

  @VisibleForTesting
  public ServiceAccountConnection getSAAvailableConnection() {
    return connectionService.getAllConnections(getContextTeamName()).stream()
        .filter(c -> c instanceof ServiceAccountConnection)
        .map(c -> (ServiceAccountConnection) c)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException("Can't initialize the fetcher: no connection found"));
  }

  private long daysToMillis(int days) {
    return System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
  }
}
