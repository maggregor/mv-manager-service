package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.ConnectionType;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import java.util.List;
import java.util.Set;
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
    try {
      return fetcher.fetchAllQueriesFrom(fromTimestamp);
    } finally {
      fetcher.close();
    }
  }

  public Set<FetchedTable> fetchAllTables(String projectId, Connection connection) {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    try {
      return fetcher.fetchAllTables();
    } finally {
      fetcher.close();
    }
  }

  public void createMaterializedView(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    try {
      fetcher.createMaterializedView(mv);
    } finally {

      fetcher.close();
    }
  }

  public void deleteMaterializedView(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    try {
      fetcher.deleteMaterializedView(mv);
    } finally {

      fetcher.close();
    }
  }

  public void dryRunQuery(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    try {
      fetcher.dryRunQuery(mv.getStatement());
    } finally {
      fetcher.close();
    }
  }

  private DatabaseFetcher fetcher(String projectId, Connection connection)
      throws ProjectNotFoundException {
    if (connection.getType() != ConnectionType.SERVICE_ACCOUNT) {
      throw new IllegalArgumentException("Connection type unknown. Should be a service account");
    }
    ServiceAccountConnection saConnection = (ServiceAccountConnection) connection;
    String serviceAccount = saConnection.getServiceAccountKey();
    return new BigQueryDatabaseFetcher(serviceAccount, projectId);
  }

  private long daysToMillis(int days) {
    return System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000;
  }
}
