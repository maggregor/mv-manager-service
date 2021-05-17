package com.alwaysmart.optimizer.databases.bigquery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedQueryFactory;
import com.alwaysmart.optimizer.databases.entities.DefaultFetchedDataset;
import com.alwaysmart.optimizer.databases.entities.DefaultFetchedProject;
import com.alwaysmart.optimizer.databases.entities.DefaultFetchedTable;
import com.alwaysmart.optimizer.databases.entities.FetchedDataset;
import com.alwaysmart.optimizer.databases.entities.FetchedProject;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.zetasql.ZetaSQLType;
import org.apache.commons.lang3.StringUtils;

import static com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper.datasetToString;
import static com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper.parseDataset;
import static com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper.parseTable;
import static com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper.tableToString;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

    private static final int LIST_JOB_PAGE_SIZE = 25000;
    private BigQuery bigquery;
    private ResourceManager resourceManager;

    public BigQueryDatabaseFetcher(GoogleCredentials googleCredentials, String projectId) {
        BigQueryOptions.Builder bqOptBuilder = BigQueryOptions
                .newBuilder()
                .setCredentials(googleCredentials);
        ResourceManagerOptions.Builder rmOptBuilder = ResourceManagerOptions
                .newBuilder()
                .setCredentials(googleCredentials);
        if(StringUtils.isNotEmpty(projectId)) {
            // Change default project of BigQuery instance
            bqOptBuilder.setProjectId(projectId);
            rmOptBuilder.setProjectId(projectId);
        }
        this.bigquery = bqOptBuilder.build().getService();
        this.resourceManager = rmOptBuilder.build().getService();
    }

    @Override
    public List<FetchedQuery> fetchQueries(String project) {
        return fetchQueries(project, null);
    }

    @Override
    public List<FetchedQuery> fetchQueries(String project, Date start) {
        List<BigQuery.JobListOption> options = new ArrayList<>();
        options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
        if (start != null) {
            options.add(BigQuery.JobListOption.minCreationTime(start.getTime()));
        }
        Page<Job> jobs = bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]));
        List<FetchedQuery> fetchedQueries = new ArrayList<>();
        for (Job job : jobs.getValues()) {
            QueryJobConfiguration queryJobConfiguration = job.getConfiguration();
            String jobQuery = queryJobConfiguration.getQuery();
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
                fetchedQueries.add(FetchedQueryFactory.createFetchedQuery(query, cost));
            }
        }
        return fetchedQueries;
    }

    @Override
    public FetchedTable fetchTable(String tableIdString) throws IllegalArgumentException {
        try {
            TableId tableId = parseTable(tableIdString);
            Table table = bigquery.getTable(tableId);
            if (
            /*
             * Element should exists.
             * Should be a table and not a View or Materialized View.
             */
            table == null

            || !table.exists()
            || !(table.getDefinition() instanceof StandardTableDefinition)) {
                return null;
            }
            StandardTableDefinition tableDefinition = table.getDefinition();
            Schema tableSchema = tableDefinition.getSchema();
            Map<String, String> tableColumns = this.fetchColumns(tableSchema.getFields());
            return new DefaultFetchedTable(tableIdString, tableId.getProject(), tableId.getDataset(), tableId.getTable(), tableColumns);
        } catch (BigQueryException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    @Override
    public List<String> fetchTableIds(String datasetIdString) {
        DatasetId datasetId = parseDataset(datasetIdString);
        List<String> tables = new ArrayList<>();
        for (Table table :  bigquery.listTables(datasetId).getValues()) {
            tables.add(tableToString(table.getTableId()));
        }
        return tables;
    }

    private Map<String, String> fetchColumns(List<Field> googleFields) {
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
    public List<String> fetchProjectIds() {
        Set<String> projects = new HashSet<>();
        for(Project project : resourceManager.list().iterateAll()) {
            projects.add(project.getProjectId());
        }
        return new ArrayList<>(projects);
    }

    @Override
    public FetchedProject fetchProject(String projectId) {
        return new DefaultFetchedProject(projectId, fetchProjectName(projectId), fetchDatasetIds(projectId));
    }

    public String fetchProjectName(String projectId) {
        for(Project project : resourceManager.list().iterateAll()) {
            if (projectId.equals(project.getProjectId())) {
                return project.getName();
            }
        }
        return null;
    }

    @Override
    public List<String> fetchDatasetIds(String projectId) {
        List<String> datasets = new ArrayList<>();
        for (Dataset dataset :  bigquery.listDatasets(projectId).getValues()) {
            datasets.add(datasetToString(dataset.getDatasetId()));
        }
        return datasets;
    }

    @Override
    public FetchedDataset fetchDataset(String datasetIdString) {
        DatasetId datasetId = parseDataset(datasetIdString);
        return new DefaultFetchedDataset(datasetIdString, datasetId.getProject(), datasetId.getDataset(), fetchTableIds(datasetIdString));
    }

}