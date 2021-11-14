package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.utils.DateUtils.getPastDate;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import java.util.Date;
import java.util.List;
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

  public List<FetchedProject> fetchAllProjects() throws Exception {
    return fetcher().fetchAllProjects();
  }

  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    return fetcher(projectId).fetchProject(projectId);
  }

  public List<FetchedDataset> fetchAllDatasets(String projectId) throws Exception {
    return fetcher(projectId).fetchAllDatasets();
  }

  public FetchedDataset fetchDataset(String projectId, String datasetName) throws Exception {
    return fetcher(projectId).fetchDataset(datasetName);
  }

  public List<FetchedQuery> fetchQueries(String projectId) throws Exception {
    return fetcher(projectId).fetchAllQueries();
  }

  public List<FetchedQuery> fetchQueriesSince(String projectId, Date date) throws Exception {
    return fetcher(projectId).fetchAllQueriesFrom(date);
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

  public GlobalQueryStatistics getStatistics(String projectId, int lastDays) {
    List<FetchedQuery> allSelect = fetcher(projectId).fetchAllQueriesFrom(getPastDate(lastDays));
    // Select using materialized view
    List<FetchedQuery> selectIn = allSelect.stream()
        .filter(FetchedQuery::isUsingMaterializedView)
        .collect(Collectors.toList());
    // Select using cache
    List<FetchedQuery> selectCached = allSelect.stream()
        .filter(FetchedQuery::isUsingCache)
        .collect(Collectors.toList());
    // Select using table source
    List<FetchedQuery> selectOut = allSelect.stream()
        .filter(q -> !q.isUsingMaterializedView() && !q.isUsingCache())
        .collect(Collectors.toList());
    //
    GlobalQueryStatistics global = new GlobalQueryStatistics();
    global.addStatistic(GlobalQueryStatistics.SCOPE_IN, new QueryStatistics(selectIn));
    global.addStatistic(GlobalQueryStatistics.SCOPE_OUT, new QueryStatistics(selectOut));
    global.addStatistic(GlobalQueryStatistics.SCOPE_CACHED, new QueryStatistics(selectCached));
    return global;
  }

  public int fetchMMVCount(String projectId) throws Exception {
    return fetcher(projectId).fetchMMVCount();
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
