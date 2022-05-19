package com.achilio.mvm.service.databases.bigquery;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableField;
import com.google.cloud.bigquery.BigQuery.TableOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.MaterializedViewDefinition;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

  public static final int LIST_JOB_PAGE_SIZE = 1000;
  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryDatabaseFetcher.class);
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

  @Override
  public FetchedProject fetchProject(String projectId) throws ProjectNotFoundException {
    projectId = projectId.toLowerCase();
    Project project = resourceManager.get(projectId);
    if (project == null) {
      throw new ProjectNotFoundException(projectId);
    }
    return toFetchedProject(project);
  }

  @Override
  public Iterable<Dataset> fetchAllDatasets(String projectId) {
    return bigquery.listDatasets(projectId).iterateAll();
  }

  @Override
  public Stream<Table> fetchTablesInDataset(String datasetName) {
    Spliterator<Table> spliterator = bigquery.listTables(datasetName).getValues().spliterator();
    return StreamSupport.stream(spliterator, true)
        .map(table -> bigquery.getTable(table.getTableId(),
            TableOption.fields(
                TableField.CREATION_TIME,
                TableField.LAST_MODIFIED_TIME,
                TableField.NUM_BYTES,
                TableField.NUM_LONG_TERM_BYTES,
                TableField.NUM_ROWS,
                TableField.SCHEMA
            )));
  }

  @Override
  public void createMaterializedView(MaterializedView mv) {
    TableId tableId = TableId.of(mv.getDatasetName(), mv.getMvName());
    MaterializedViewDefinition materializedViewDefinition =
        MaterializedViewDefinition.newBuilder(mv.getStatement()).build();
    TableInfo tableInfo = TableInfo.newBuilder(tableId, materializedViewDefinition).build();
    try {
      bigquery.create(tableInfo);
    } catch (BigQueryException e) {
      if (!e.getError().getReason().equals("duplicate")) {
        throw e;
      } else {
        LOGGER.info("Materialized View {} already exists. Nothing to do", mv.getMvUniqueName());
      }
    }
  }

  @Override
  public void deleteMaterializedView(MaterializedView mv) {
    TableId tableId = TableId.of(mv.getDatasetName(), mv.getMvName());
    bigquery.delete(tableId);
  }

  public void dryRunQuery(String query) {
    try {
      QueryJobConfiguration queryConfig =
          QueryJobConfiguration.newBuilder(query).setDryRun(true).setUseQueryCache(false).build();
      bigquery.create(JobInfo.of(queryConfig));
    } catch (BigQueryException e) {
      LOGGER.warn(
          "Query failed: {}: {}: {}",
          e.getReason(),
          e.getMessage(),
          query.trim().replaceAll("[\r\n]+", ""));
      throw e;
    }
  }

  public Iterable<Job> fetchJobIterable(long fromTimestamp) {
    List<BigQuery.JobListOption> options = getJobListOptions(fromTimestamp);
    return bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0])).iterateAll();
  }

  private List<BigQuery.JobListOption> getJobListOptions(long fromCreationTime) {
    List<BigQuery.JobListOption> options = new ArrayList<>();
    options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
    options.add(BigQuery.JobListOption.allUsers());
    options.add(BigQuery.JobListOption.minCreationTime(fromCreationTime));
    return options;
  }

  public FetchedProject toFetchedProject(Project project) {
    return new DefaultFetchedProject(project.getProjectId(), project.getName());
  }
}
