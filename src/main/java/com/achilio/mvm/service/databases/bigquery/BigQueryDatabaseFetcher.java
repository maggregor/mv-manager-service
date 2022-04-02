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
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.visitors.ATableId;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableField;
import com.google.cloud.bigquery.BigQuery.TableOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.zetasql.ZetaSQLType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

  public static final int LIST_JOB_PAGE_SIZE = 1000;
  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryDatabaseFetcher.class);
  private static final String SQL_FROM_WORD = "FROM";
  private final BigQuery bigquery;
  private final ResourceManager resourceManager;

  @VisibleForTesting
  public BigQueryDatabaseFetcher(BigQuery bigquery, ResourceManager rm) {
    this.bigquery = bigquery;
    this.resourceManager = rm;
  }

  public BigQueryDatabaseFetcher(final String serviceAccount, final String projectId) {
    GoogleCredentials credentials;
    try {
      credentials =
          GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccount.getBytes()));

    } catch (IOException e) {
      LOGGER.error("Cannot read service account {}", serviceAccount);
      throw new RuntimeException(e);
    }
    BigQueryOptions.Builder bqOptBuilder = BigQueryOptions.newBuilder().setCredentials(credentials);
    ResourceManagerOptions.Builder rmOptBuilder =
        ResourceManagerOptions.newBuilder().setCredentials(credentials);
    if (StringUtils.isNotEmpty(projectId)) {
      // Change default project of BigQuery instance
      bqOptBuilder.setProjectId(projectId);
      rmOptBuilder.setProjectId(projectId);
    }
    this.bigquery = bqOptBuilder.build().getService();
    this.resourceManager = rmOptBuilder.build().getService();
    // Checks if the Google credentials have access.
    if (StringUtils.isNotEmpty(projectId)) {
      fetchProject(projectId);
    }
  }

  public BigQueryDatabaseFetcher(final GoogleCredentials credentials, final String projectId)
      throws ProjectNotFoundException {
    BigQueryOptions.Builder bqOptBuilder = BigQueryOptions.newBuilder().setCredentials(credentials);
    ResourceManagerOptions.Builder rmOptBuilder =
        ResourceManagerOptions.newBuilder().setCredentials(credentials);
    if (StringUtils.isNotEmpty(projectId)) {
      // Change default project of BigQuery instance
      bqOptBuilder.setProjectId(projectId);
      rmOptBuilder.setProjectId(projectId);
    }
    this.bigquery = bqOptBuilder.build().getService();
    this.resourceManager = rmOptBuilder.build().getService();
    // Checks if the Google credentials have access.
    if (StringUtils.isNotEmpty(projectId)) {
      fetchProject(projectId);
    }
  }

  @Override
  public List<FetchedQuery> fetchAllQueries() {
    return fetchAllQueriesFrom(0);
  }

  @Override
  public List<FetchedQuery> fetchAllQueriesFrom(long fromCreationTime) {
    return fetchJobs(fromCreationTime)
        .filter(this::isValidQueryJob)
        .map(this::toFetchedQuery)
        .collect(Collectors.toList());
  }

  @Override
  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    Project project = resourceManager.get(projectId);
    if (project == null) {
      throw new ProjectNotFoundException(projectId);
    }
    return toFetchedProject(project);
  }

  @Override
  public List<FetchedDataset> fetchAllDatasets(String projectId) {
    List<FetchedDataset> datasets = new ArrayList<>();
    for (Dataset dataset : bigquery.listDatasets(projectId).iterateAll()) {
      datasets.add(toFetchedDataset(dataset));
    }
    return datasets;
  }

  @Override
  public FetchedDataset fetchDataset(String datasetName) {
    Dataset dataset = bigquery.getDataset(datasetName);
    return toFetchedDataset(dataset);
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
  public Set<FetchedTable> fetchTablesInDataset(String datasetName) {
    Spliterator<Table> spliterator = bigquery.listTables(datasetName).getValues().spliterator();
    return StreamSupport.stream(spliterator, true)
        .map(table -> bigquery.getTable(table.getTableId(), TableOption.fields(TableField.SCHEMA)))
        .filter(this::isValidTable)
        .map(this::toFetchedTable)
        .collect(Collectors.toSet());
  }

  public void close() {}

  private Stream<Job> fetchJobs(long fromCreationTime) {
    List<BigQuery.JobListOption> options = getJobListOptions(fromCreationTime);
    final Page<Job> jobPages = bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]));
    return StreamSupport.stream(jobPages.iterateAll().spliterator(), true);
  }

  /** Returns true if a job is a query job */
  public boolean isValidQueryJob(Job job) {
    return Objects.nonNull(job) && isQueryJob(job) && notInError(job);
  }

  public boolean notInError(Job job) {
    return job.getStatus().getError() == null;
  }

  public boolean isQueryJob(Job job) {
    return job.getConfiguration() instanceof QueryJobConfiguration;
  }

  /**
   * Convert a QueryJob (Google) to a FetchedQuery. Retrieve some metrics google side (processed
   * bytes, cache using...)
   */
  private FetchedQuery toFetchedQuery(Job job) {
    String query;
    final QueryJobConfiguration configuration = job.getConfiguration();
    query = StringUtils.trim(configuration.getQuery());
    DatasetId dataset = configuration.getDefaultDataset();
    final JobStatistics.QueryStatistics stats = job.getStatistics();
    Long startTime = stats.getStartTime();
    final boolean useCache = BooleanUtils.isTrue(stats.getCacheHit());
    final boolean usingManagedMV = containsManagedMVUsageInQueryStages(stats.getQueryPlan());
    FetchedQuery fetchedQuery =
        FetchedQueryFactory.createFetchedQuery(
            job.getJobId().getProject(), StringUtils.trim(query));
    fetchedQuery.setStartTime(startTime);
    fetchedQuery.setStatistics(toQueryUsageStatistics(stats));
    fetchedQuery.setUseMaterializedView(usingManagedMV);
    fetchedQuery.setUseCache(useCache);
    fetchedQuery.setGoogleJobId(job.getJobId().getJob());
    fetchedQuery.setDefaultDataset(dataset == null ? null : dataset.getDataset());
    return fetchedQuery;
  }

  public QueryUsageStatistics toQueryUsageStatistics(
      JobStatistics.QueryStatistics queryStatistics) {
    QueryUsageStatistics statistics = new QueryUsageStatistics();
    if (queryStatistics.getTotalBytesProcessed() != null) {
      statistics.setProcessedBytes(queryStatistics.getTotalBytesProcessed());
    }
    if (queryStatistics.getTotalBytesBilled() != null) {
      statistics.setBilledBytes(queryStatistics.getTotalBytesBilled());
    }
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
        .anyMatch(subStep -> subStep.contains(SQL_FROM_WORD) && (subStep.contains("achilio_")));
  }

  private List<BigQuery.JobListOption> getJobListOptions(long fromCreationTime) {
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(fromCreationTime));
    return options;
  }

  private FetchedTable toFetchedTable(Table table) {
    StandardTableDefinition tableDefinition = table.getDefinition();
    Map<String, String> tableColumns = mapColumnsOrEmptyIfSchemaIsNull(tableDefinition);
    ATableId aTableId = ATableId.fromGoogleTableId(table.getTableId());
    return new DefaultFetchedTable(aTableId, tableColumns);
  }

  /**
   * Returns true if the table is eligible
   *
   * <p>- Don't have RECORD field type
   */
  private boolean isEligibleTableDefinition(StandardTableDefinition tableDefinition) {
    return tableDefinition.getSchema() != null
        && tableDefinition.getSchema().getFields().stream()
            .noneMatch(f -> f.getType().equals(LegacySQLTypeName.RECORD));
  }

  /*
   * Filter on:
   * - should exists.
   * - should be a StandardTableDefinition (and not a View or Materialized View).
   */
  public boolean isValidTable(Table table) {
    return table != null
        && table.exists()
        && table.getDefinition() instanceof StandardTableDefinition
        && isEligibleTableDefinition(table.getDefinition());
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
        datasetId.toString(),
        location,
        friendlyName,
        description,
        createdAt,
        lastModified);
  }
}
