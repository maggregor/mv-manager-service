package com.achilio.mvm.service.databases.bigquery;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.entities.DefaultFetchedDataset;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.databases.entities.DefaultFetchedTable;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedMaterializedViewEvent;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableField;
import com.google.cloud.bigquery.BigQuery.TableOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryDatabaseFetcher.class);

  private static final String UNSUPPORTED_TABLE_TOKEN = "INFORMATION_SCHEMA";
  private static final String SQL_FROM_WORD = "FROM";
  private static final String SQL_SELECT_WORD = "SELECT";
  private static final int LIST_JOB_PAGE_SIZE = 10000;
  private final BigQuery bigquery;
  private final ResourceManager resourceManager;
  private final String projectId;

  @VisibleForTesting
  public BigQueryDatabaseFetcher(BigQuery bigquery, ResourceManager rm, String projectId) {
    this.bigquery = bigquery;
    this.resourceManager = rm;
    this.projectId = projectId;
  }

  public BigQueryDatabaseFetcher(final GoogleCredentials credentials, final String defaultProjectId)
      throws ProjectNotFoundException {
    BigQueryOptions.Builder bqOptBuilder = BigQueryOptions.newBuilder().setCredentials(credentials);
    ResourceManagerOptions.Builder rmOptBuilder =
        ResourceManagerOptions.newBuilder().setCredentials(credentials);
    if (StringUtils.isNotEmpty(defaultProjectId)) {
      // Change default project of BigQuery instance
      bqOptBuilder.setProjectId(defaultProjectId);
      rmOptBuilder.setProjectId(defaultProjectId);
    }
    this.bigquery = bqOptBuilder.build().getService();
    this.resourceManager = rmOptBuilder.build().getService();
    this.projectId = defaultProjectId;
    // Checks if the Google credentials have access.
    if (StringUtils.isNotEmpty(projectId)) {
      fetchProject(projectId);
    }
  }

  @Override
  public List<FetchedQuery> fetchAllQueries() {
    return fetchAllQueriesFrom(0);
  }

  private Stream<Job> fetchJobs(long fromCreationTime) {
    List<BigQuery.JobListOption> options = getJobListOptions(fromCreationTime);
    final Page<Job> jobPages = bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]));
    return StreamSupport.stream(jobPages.getValues().spliterator(), true);
  }

  @Override
  public List<FetchedQuery> fetchAllQueriesFrom(long fromCreationTime) {
    return fetchJobs(fromCreationTime)
        .filter(this::fetchQueryFilter)
        .map(this::toFetchedQuery)
        .collect(Collectors.toList());
  }

  /**
   * Returns true if a job is a query job. Only SELECT finished queries on regular tables.
   *
   * @param job - A Job from BigQuery job history
   * @return
   */
  public boolean fetchQueryFilter(Job job) {
    if (Objects.nonNull(job) && isQueryJob(job)) {
      QueryJobConfiguration configuration = job.getConfiguration();
      final String query = configuration.getQuery();
      // Exclude SQL script
      return !isSQLScript(query)
          // Exclude not finished queries
          //  && job.isDone()
          // Exclude errors
          && notInError(job)
          // Keep only SELECT queries
          && isRegularSelectQuery(query);
    }
    return false;
  }

  public boolean notInError(Job job) {
    return job.getStatus().getError() == null;
  }

  public boolean isQueryJob(Job job) {
    return job.getConfiguration() instanceof QueryJobConfiguration;
  }

  public boolean isRegularSelectQuery(String query) {
    // Remove comments
    query = query.replaceAll("--[^\\r\\n]*", "");
    query = query.trim();
    return StringUtils.startsWithIgnoreCase(query, SQL_SELECT_WORD)
        && StringUtils.containsIgnoreCase(query, SQL_FROM_WORD)
        && !StringUtils.containsIgnoreCase(query, UNSUPPORTED_TABLE_TOKEN);
  }

  /**
   * Returns true if the query job contains more than one query
   *
   * @return
   */
  public boolean isSQLScript(final String query) {
    return StringUtils.isNotEmpty(query) && query.trim().split(";").length > 1;
  }

  /**
   * Convert a QueryJob (Google) to a FetchedQuery. Retrieve some metrics google side (processed
   * bytes, cache using...)
   *
   * @param job
   * @return
   */
  private FetchedQuery toFetchedQuery(Job job) {
    String query;
    final QueryJobConfiguration configuration = job.getConfiguration();
    query = StringUtils.trim(configuration.getQuery());
    final JobStatistics.QueryStatistics stats = job.getStatistics();
    final boolean useCache = BooleanUtils.isTrue(stats.getCacheHit());
    final boolean usingManagedMV = containsManagedMVUsageInQueryStages(stats.getQueryPlan());
    FetchedQuery fetchedQuery = FetchedQueryFactory.createFetchedQuery(StringUtils.trim(query));
    fetchedQuery.setStatistics(toQueryUsageStatistics(stats));
    fetchedQuery.setUseMaterializedView(usingManagedMV);
    fetchedQuery.setUseCache(useCache);
    return fetchedQuery;
  }

  public QueryUsageStatistics toQueryUsageStatistics(
      JobStatistics.QueryStatistics queryStatistics) {
    QueryUsageStatistics statistics = new QueryUsageStatistics();
    statistics.setProcessedBytes(queryStatistics.getTotalBytesProcessed());
    statistics.setBilledBytes(queryStatistics.getTotalBytesBilled());
    return statistics;
  }

  public boolean containsManagedMVUsageInQueryStages(List<QueryStage> stages) {
    if (stages == null) {
      LOGGER.debug("Skipped plan analysis: the stage is null");
      return false;
    }
    for (QueryStage queryStage : stages) {
      for (QueryStage.QueryStep queryStep : queryStage.getSteps()) {
        if (containsSubStepUsingMVM(queryStep)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * In the query substeps, filter only the steps that hit a managed materialized view (mmv)
   *
   * @param step - QueryStage#QueryStep from fetched BigQuery history which contains subSteps.
   * @return boolean - True if the query plan used a MVM.
   */
  public boolean containsSubStepUsingMVM(QueryStep step) {
    return step.getSubsteps().stream()
        .anyMatch(subStep -> subStep.contains(SQL_FROM_WORD) && subStep.contains("mvm_"));
  }

  private List<BigQuery.JobListOption> getJobListOptions(long fromCreationTime) {
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(fromCreationTime));
    return options;
  }

  @Override
  public FetchedTable fetchTable(String datasetName, String tableName)
      throws IllegalArgumentException {
    try {
      TableId tableId = TableId.of(datasetName, tableName);
      Table table = bigquery.getTable(tableId);
      if (!isValidTable(table)) {
        LOGGER.warn("Fetched table is not valid: {}", table);
        return null;
      }
      return toFetchedTable(table);
    } catch (BigQueryException e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  private FetchedTable toFetchedTable(Table table) {
    StandardTableDefinition tableDefinition = table.getDefinition();
    Map<String, String> tableColumns = mapColumnsOrEmptyIfSchemaIsNull(tableDefinition);
    TableId tableId = table.getTableId();
    String projectId = tableId.getProject();
    String datasetName = tableId.getDataset();
    String tableName = tableId.getTable();
    return new DefaultFetchedTable(projectId, datasetName, tableName, tableColumns);
  }

  @Override
  public Set<FetchedTable> fetchAllTables() {
    Spliterator<Dataset> spliterator = bigquery.listDatasets().getValues().spliterator();
    return StreamSupport.stream(spliterator, true)
        .map(dataset -> dataset.getDatasetId().getDataset())
        .map(this::fetchTablesInDataset)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<FetchedTable> fetchTableNamesInDataset(String datasetName) {
    Spliterator<Table> spliterator = bigquery.listTables(datasetName).getValues().spliterator();
    return StreamSupport.stream(spliterator, true)
        .filter(this::isValidTable)
        .map(this::toFetchedTable)
        .collect(Collectors.toSet());
  }

  @Override
  public List<FetchedMaterializedViewEvent> fetchMaterializedViewEvents(long fromDate) {
    return fetchJobs(fromDate)
        .filter(this::filterMaterializedViewCreation)
        .map(this::toFetchedMaterializedViewEvent)
        .collect(Collectors.toList());
  }

  private FetchedMaterializedViewEvent toFetchedMaterializedViewEvent(Job job) {
    final QueryJobConfiguration configuration = job.getConfiguration();
    final String query = configuration.getQuery();
    final String operationType = query.split("\\s+")[0];
    return new FetchedMaterializedViewEvent("unknown_name",
        "unknown_dataset", "unknown_table", 0, operationType);
  }

  private boolean filterMaterializedViewCreation(Job job) {
    final QueryJobConfiguration configuration = job.getConfiguration();
    final String query = configuration.getQuery();

    return StringUtils.containsIgnoreCase(query, "MATERIALIZED VIEW")
        && !StringUtils.startsWithIgnoreCase(query, "CALL");
  }

  @Override
  public Set<FetchedTable> fetchTablesInDataset(String datasetName) {
    Spliterator<Table> spliterator = bigquery.listTables(datasetName).getValues().spliterator();
    return StreamSupport.stream(spliterator, true)
        .map(table -> bigquery.getTable(table.getTableId(), TableOption.fields(TableField.SCHEMA)))
        .filter(this::isValidTable)
        .map(this::toFetchedTable)
        .collect(Collectors.toSet());
  }

  /*
   * Filter on:
   * - should exists.
   * - should be a StandardTableDefinition (and not a View or Materialized View).
   */
  public boolean isValidTable(Table table) {
    return table != null
        && table.exists()
        && table.getDefinition() instanceof StandardTableDefinition;
  }

  private Map<String, String> mapColumnsOrEmptyIfSchemaIsNull(TableDefinition definition) {
    final Schema schema = definition.getSchema();
    if (schema == null) {
      LOGGER.warn("Can't retrieve columns: schema is null");
      return new HashMap<>();
    }
    List<Field> fields = schema.getFields();
    return fields.stream().collect(Collectors.toMap(Field::getName, this::toZetaSQLStringType));
  }

  private String toZetaSQLStringType(Field field) {
    final String statusType = field.getType().toString();
    switch (statusType) {
      case "DOUBLE":
      case "FLOAT":
        return ZetaSQLType.TypeKind.TYPE_NUMERIC.name();
      case "INTEGER":
        return ZetaSQLType.TypeKind.TYPE_INT64.name();
      case "BOOLEAN":
        return ZetaSQLType.TypeKind.TYPE_BOOL.name();
      default:
        return "TYPE_" + statusType;
    }
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
