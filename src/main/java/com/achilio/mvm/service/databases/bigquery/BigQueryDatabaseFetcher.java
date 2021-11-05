package com.achilio.mvm.service.databases.bigquery;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.entities.DefaultFetchedDataset;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.collect.Lists;
import com.google.zetasql.ZetaSQLType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryDatabaseFetcher.class);

  private static final int LIST_JOB_PAGE_SIZE = 25000;
  private final BigQuery bigquery;
  private final ResourceManager resourceManager;
  private final String projectId;

  public BigQueryDatabaseFetcher(GoogleCredentials googleCredentials, String projectId)
      throws ProjectNotFoundException {
    BigQueryOptions.Builder bqOptBuilder =
        BigQueryOptions.newBuilder().setCredentials(googleCredentials);
    ResourceManagerOptions.Builder rmOptBuilder =
        ResourceManagerOptions.newBuilder().setCredentials(googleCredentials);
    if (StringUtils.isNotEmpty(projectId)) {
      // Change default project of BigQuery instance
      bqOptBuilder.setProjectId(projectId);
      rmOptBuilder.setProjectId(projectId);
    }
    this.bigquery = bqOptBuilder.build().getService();
    this.resourceManager = rmOptBuilder.build().getService();
    this.projectId = projectId;
    // Checks if the Google credentials have access.
    if (StringUtils.isNotEmpty(projectId)) {
      fetchProject(projectId);
    }
  }

  @Override
  public List<FetchedQuery> fetchAllQueries() {
    return fetchAllQueriesFrom(null);
  }

  @Override
  public List<FetchedQuery> fetchAllQueriesFrom(Date start) {
    List<BigQuery.JobListOption> options = defaultJobListOptions();
    options.add(BigQuery.JobListOption.allUsers());
    if (start != null) {
      options.add(BigQuery.JobListOption.minCreationTime(start.getTime()));
    }
    return fetchQueries(options);
  }

  private List<FetchedQuery> fetchQueries(List<BigQuery.JobListOption> options) {
    Page<Job> jobs = bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]));
    List<FetchedQuery> fetchedQueries = new ArrayList<>();
    for (Job job : jobs.getValues()) {
      if (job.getConfiguration() instanceof QueryJobConfiguration) {
        QueryJobConfiguration queryJobConfiguration = job.getConfiguration();
        String jobQuery = queryJobConfiguration.getQuery();
        // Should we optimize query sent like SQL script ?
        // TODO: Split hard by ; really sure?
        String[] queries = jobQuery.split(";");

          for (String query : queries) {
            if (!jobQuery.toUpperCase().startsWith("SELECT")
                    || jobQuery.toUpperCase().contains("INFORMATION_SCHEMA")) {
              continue;
            }
            JobStatistics.QueryStatistics queryStatistics = job.getStatistics();
            Long billed = queryStatistics.getTotalBytesBilled();
            long cost = billed == null ? -1 : billed;
            boolean usingManagedMV = containsMVManagedUsageInQueryStages(queryStatistics.getQueryPlan());
            FetchedQuery fetchedQuery = FetchedQueryFactory.createFetchedQuery(query, cost);
            fetchedQuery.setProjectId(projectId);
            fetchedQuery.setUsingManagedMV(usingManagedMV);
            fetchedQueries.add(fetchedQuery);
           //Lists.transform(queryStatistics.getQueryPlan(), QueryStage.);
          }
      }
    }
    return fetchedQueries;
  }

  private boolean containsMVManagedUsageInQueryStages(List<QueryStage> stages) {
    try {
      if (stages == null) {
        LOGGER.error("Skipped plan analysis: no stages");
        return false;
      }
      for (QueryStage queryStage : stages) {
        if (queryStage == null || queryStage.getSteps() == null) {
          LOGGER.error("Skipped plan analysis: no steps");
          return false;
        }
        for (QueryStage.QueryStep queryStep : queryStage.getSteps()) {
          if (queryStep.getSubsteps() == null) {
            LOGGER.error("Skipped plan analysis: no substeps");
            return false;
          }
          for (String subStep : queryStep.getSubsteps()) {
            return StringUtils.containsIgnoreCase(subStep, "FROM")
                    && StringUtils.containsIgnoreCase(subStep, "mvm_");
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private List<BigQuery.JobListOption> defaultJobListOptions() {
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
    return options;
  }

  @Override
  public FetchedTable fetchTable(String projectId, String datasetName, String tableName)
      throws IllegalArgumentException {
    try {
      TableId tableId = TableId.of(datasetName, tableName);
      Table table = bigquery.getTable(tableId);
      if (!isValidTable(table)) {
        return null;
      }
      return toFetchedTable(table);
    } catch (BigQueryException e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  private FetchedTable toFetchedTable(Table table) {
    StandardTableDefinition tableDefinition = table.getDefinition();
    Schema tableSchema = tableDefinition.getSchema();
    assert tableSchema != null;
    Map<String, String> tableColumns = mapColumns(tableSchema.getFields());
    TableId tableId = table.getTableId();
    return new DefaultFetchedTable(
        tableId.getProject(), tableId.getDataset(), tableId.getTable(), tableColumns);
  }

  @Override
  public List<FetchedTable> fetchAllTables() {
    List<FetchedTable> tables = new ArrayList<>();
    bigquery
        .listDatasets()
        .getValues()
        .forEach(
            dataset -> {
              final String datasetName = dataset.getDatasetId().getDataset();
              List<FetchedTable> fetchedTables = fetchTablesInDataset(datasetName);
              tables.addAll(fetchedTables);
            });
    return tables;
  }
  public List<FetchedTable> fetchTableNamesInDataset(String datasetName) {
    List<FetchedTable> tables = new LinkedList<>();
    bigquery
            .listTables(datasetName)
            .getValues()
            .forEach(
                    tableName -> {
                        tables.add(new DefaultFetchedTable(projectId, datasetName, tableName.getFriendlyName()));
                    });
    return tables;
  }

    public List<FetchedTable> fetchTablesInDataset(String datasetName) {
    List<FetchedTable> tables = new LinkedList<>();
    bigquery
        .listTables(datasetName)
        .getValues()
        .forEach(
            table -> {
              // Force in order to retrieve metadata (ie: schema)
              table = bigquery.getTable(table.getTableId());
              if (isValidTable(table)) {
                tables.add(toFetchedTable(table));
              }
            });
    return tables;
  }

  @Override
  public int fetchMMVCount(String projectId) {
    // TODO: Temporary
    return RandomUtils.nextInt(0, 20);
  }

  @Override
  public long totalScannedBytesSince(String projectId, ZonedDateTime time) {
    // TODO: Temporary
    return RandomUtils.nextLong(10_000_000_000L, 1_000_000_000_000L);
  }

  /*
   * Filter on:
   * - should exists.
   * - should be a StandardTableDefinition (and not a View or Materialized View).
   */
  private boolean isValidTable(Table table) {
    return table != null
        && table.exists()
        && table.getDefinition() instanceof StandardTableDefinition;
  }

  private Map<String, String> mapColumns(List<Field> googleFields) {
    Map<String, String> tableColumns = new HashMap<>();
    for (Field field : googleFields) {
      String fetchedType = field.getType().toString();
      String type = convertToZetaSQLType(fetchedType);
      tableColumns.put(field.getName(), type);
    }
    return tableColumns;
  }

  private String convertToZetaSQLType(String fetchedType) {
    fetchedType = fetchedType.toUpperCase();
    switch (fetchedType) {
      case "DOUBLE":
      case "FLOAT":
        fetchedType = ZetaSQLType.TypeKind.TYPE_NUMERIC.name();
        break;
      case "INTEGER":
        fetchedType = ZetaSQLType.TypeKind.TYPE_INT64.name();
        break;
      case "BOOLEAN":
        fetchedType = ZetaSQLType.TypeKind.TYPE_BOOL.name();
        break;
      default:
        fetchedType = "TYPE_" + fetchedType;
        break;
    }
    return fetchedType;
  }

  @Override
  public List<FetchedProject> fetchAllProjects() {
    List<FetchedProject> projects = new ArrayList<>();
    for (Project project : resourceManager.list().iterateAll()) {
      projects.add(toFetchedProject(project));
    }
    return projects;
  }

  @Override
  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    Project project = resourceManager.get(projectId);
    if (project == null) {
      throw new ProjectNotFoundException(projectId + " not found");
    }
    return toFetchedProject(project);
  }

  @Override
  public List<FetchedDataset> fetchAllDatasets() {
    List<FetchedDataset> datasets = new ArrayList<>();
    for (Dataset dataset : bigquery.listDatasets().iterateAll()) {
      datasets.add(toFetchedDataset(dataset));
    }
    return datasets;
  }

  @Override
  public FetchedDataset fetchDataset(String datasetName) {
    Dataset dataset = bigquery.getDataset(datasetName);
    return toFetchedDataset(dataset);
  }

  public FetchedProject toFetchedProject(Project project) {
    return new DefaultFetchedProject(project.getProjectId(), project.getName());
  }

  public FetchedDataset toFetchedDataset(Dataset dataset) {
    DatasetId datasetId = dataset.getDatasetId();
    final String location = dataset.getLocation();
    final String friendlyName = dataset.getFriendlyName();
    final String description = dataset.getDescription();
    final Long createdAt = dataset.getCreationTime();
    final Long lastModified = dataset.getLastModified();
    return new DefaultFetchedDataset(
        datasetId.getProject(),
        datasetId.getDataset(),
        location,
        friendlyName,
        description,
        createdAt,
        lastModified);
  }
}
