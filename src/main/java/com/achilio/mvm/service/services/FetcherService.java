package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.ConnectionType;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.Table;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class FetcherService {

  BigQueryMaterializedViewStatementBuilder statementBuilder;
  @Autowired
  ProjectService projectService;

  public FetcherService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
  }

  public FetchedProject fetchProject(String projectId, Connection connection)
      throws ProjectNotFoundException {
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    return fetcher.fetchProject(projectId);
  }

  public Iterable<Dataset> fetchAllDatasets(String projectId) {
    Connection connection = projectService.getProject(projectId).getConnection();
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    return fetcher.fetchAllDatasets(projectId);
  }

  public Stream<Table> fetchAllTables(String projectId, String datasetName) {
    Connection connection = projectService.getProject(projectId).getConnection();
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    return fetcher.fetchTablesInDataset(datasetName);
  }

  public Iterable<Job> fetchJobIterable(String projectId, int days) {
    long fromTimestamp = daysToMillis(days);
    Connection connection = projectService.getProject(projectId).getConnection();
    DatabaseFetcher fetcher = fetcher(projectId, connection);
    return fetcher.fetchJobIterable(fromTimestamp);
  }

  public void createMaterializedView(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    fetcher.createMaterializedView(mv);
  }

  public void deleteMaterializedView(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    fetcher.deleteMaterializedView(mv);
  }

  public void dryRunQuery(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    fetcher.dryRunQuery(mv.getStatement());
  }

  public void dryRunCreateMV(MaterializedView mv, Connection connection) {
    DatabaseFetcher fetcher = fetcher(mv.getProjectId(), connection);
    fetcher.dryRunQuery(mv.generateCreateStatement());
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
