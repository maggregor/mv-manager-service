package com.achilio.mvm.service.services;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** All the useful services to generate relevant Materialized Views. */
@Service
public class FetcherService {

  BigQueryMaterializedViewStatementBuilder statementBuilder;

  @PersistenceContext private EntityManager entityManager;

  public FetcherService() {
    this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
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

  public FetchedDataset fetchDataset(String projectId, String datasetName) throws Exception{
    return fetcher(projectId).fetchDataset(datasetName);
  }

  public List<FetchedQuery> fetchQueries(String projectId) throws Exception{
    return fetcher(projectId).fetchAllQueries();
  }

  public FetchedTable fetchTable(String projectId, String datasetName, String tableName) throws Exception{
    return fetcher(projectId).fetchTable(projectId, datasetName, tableName);
  }

  public List<FetchedTable> fetchAllTables(String projectId) throws Exception {
    return fetcher(projectId).fetchAllTables();
  }

  public List<FetchedTable> fetchTableNamesInDataset(String projectId, String datasetName) throws Exception {
    return fetcher(projectId).fetchTableNamesInDataset(datasetName);
  }

  public int fetchMMVCount(String projectId) throws Exception {
    return fetcher(projectId).fetchMMVCount(projectId);
  }

  public int fetchScannedBytes(String projectId) throws Exception {
    return fetcher(projectId).fetchMMVCount(projectId);
  }

  private DatabaseFetcher fetcher() throws Exception {
    return fetcher(null);
  }

  private DatabaseFetcher fetcher(String projectId) throws ProjectNotFoundException {
    SimpleGoogleCredentialsAuthentication authentication =
        (SimpleGoogleCredentialsAuthentication)
            SecurityContextHolder.getContext().getAuthentication();
    return new BigQueryDatabaseFetcher(authentication.getCredentials(), projectId);
  }
}
