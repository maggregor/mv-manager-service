package com.alwaysmart.optimizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.Table;

public class BigQueryDatabaseFetcher implements DatabaseFetcher {

    private static final int LIST_JOB_PAGE_SIZE = 25;
    private BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();


    public BigQueryDatabaseFetcher(/* Connection BigQuery context parameters */) {
        // TODO: Support users credentials
    }

    @Override
    public List<FetchedQuery> fetchQueries(String datasetName, String tableName) {
        return fetchQueries(datasetName, tableName, null);
    }

    @Override
    public List<FetchedQuery> fetchQueries(String datasetName, String tableName, Date start) {
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        List<BigQuery.JobListOption> options = new ArrayList<>();
        options.add(BigQuery.JobListOption.pageSize(LIST_JOB_PAGE_SIZE));
        if (start != null) {
            options.add(BigQuery.JobListOption.minCreationTime(start.getTime()));
        }
        Page<Job> jobs = bigquery.listJobs(options.toArray(new BigQuery.JobListOption[0]));
        List<FetchedQuery> fetchedQueries = new ArrayList<>();
        for (Job job : jobs.getValues()) {
            String query = ((QueryJobConfiguration) job.getConfiguration()).getQuery();
            JobStatistics.QueryStatistics queryStatistics = job.getStatistics();
            fetchedQueries.add(FetchedQueryFactory.createFetchedQuery(query, queryStatistics.getTotalBytesBilled()));
        }
        return fetchedQueries;
    }

    @Override
    public TableMetadata fetchTableMetadata(String datasetName, String tableName) throws IllegalArgumentException {
        try {
            TableId tableId = TableId.of(datasetName, tableName);
            Table table = bigquery.getTable(tableId);
            StandardTableDefinition tableDefinition = table.getDefinition();
            Schema tableSchema = tableDefinition.getSchema();
            Map<String, String> tableColumns = this.fetchColumns(tableSchema.getFields());
            return new DefaultTableMetadata(datasetName, tableName, tableColumns);
        } catch (BigQueryException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    private Map<String, String> fetchColumns(List<Field> googleFields) {
        Map<String, String> tableColumns = new HashMap<>();
        for (Field field : googleFields) {
            tableColumns.put(field.getName(), field.getType().toString());
        }
        return tableColumns;
    }
}