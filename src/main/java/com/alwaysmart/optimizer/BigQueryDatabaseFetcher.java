package com.alwaysmart.optimizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zetasql.ZetaSQLType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import static com.alwaysmart.optimizer.BigQueryHelper.datasetToString;
import static com.alwaysmart.optimizer.BigQueryHelper.parseDataset;
import static com.alwaysmart.optimizer.BigQueryHelper.parseTable;
import static com.alwaysmart.optimizer.BigQueryHelper.tableToString;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

    private static final int LIST_JOB_PAGE_SIZE = 25000;
    private BigQuery bigquery;
    private ResourceManager resourceManager;

    @Autowired
    OAuth2AuthorizedClientService clientService;

    public BigQueryDatabaseFetcher(GoogleCredentials googleCredentials) {
        bigquery = BigQueryOptions.newBuilder().setCredentials(googleCredentials).build().getService();
        resourceManager = ResourceManagerOptions.newBuilder().setCredentials(googleCredentials).build().getService();
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
            String query = ((QueryJobConfiguration) job.getConfiguration()).getQuery();
            if (!query.toUpperCase().startsWith("SELECT") || query.toUpperCase().contains("INFORMATION_SCHEMA")) {
                continue;
            }
            JobStatistics.QueryStatistics queryStatistics = job.getStatistics();
            Long billed = queryStatistics.getTotalBytesBilled();
            long cost = billed == null ? -1 : billed;
            fetchedQueries.add(FetchedQueryFactory.createFetchedQuery(query, cost));
            System.out.println(query.replaceAll("\n", " "));
        }
        return fetchedQueries;
    }

    @Override
    public TableMetadata fetchTable(String tableIdString) throws IllegalArgumentException {
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
            final GsonBuilder builder = new GsonBuilder();
            final Gson gson = builder.create();
            System.out.println(gson.toJson(tableSchema));
            Map<String, String> tableColumns = this.fetchColumns(tableSchema.getFields());
            return new DefaultTableMetadata(tableIdString, tableId.getProject(), tableId.getDataset(), tableId.getTable(), tableColumns);
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
            projects.add(project.getName());
        }
        return new ArrayList<>(projects);
    }

    @Override
    public ProjectMetadata fetchProject(String projectName) {
        return new DefaultProjectMetadata(projectName, fetchDatasetIds(projectName));
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
    public DatasetMetadata fetchDataset(String datasetIdString) {
        DatasetId datasetId = parseDataset(datasetIdString);
        return new DefaultDatasetMetadata(datasetIdString, datasetId.getProject(), datasetId.getDataset(), fetchTableIds(datasetIdString));
    }

}