package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BigQueryJobProcessor implements ItemProcessor<Job, Query> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryJobProcessor.class);
  private static final String SQL_FROM_WORD = "FROM";

  @Override
  public Query process(Job job) {
    return toAchilioQuery(toFetchedQuery(job));
  }

  /**
   * Convert a QueryJob (Google) to a FetchedQuery. Retrieve some metrics google side (processed
   * bytes, cache using...)
   */
  private FetchedQuery toFetchedQuery(Job job) {
    String query;
    if (!isValidQueryJob(job)) {
      return null;
    }
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
        .anyMatch(subStep -> subStep.contains(SQL_FROM_WORD) && (subStep.contains("achilio_mv_")));
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

  private Query toAchilioQuery(FetchedQuery fetchedQuery) {
    if (fetchedQuery == null) {
      return null;
    }
    return new Query(
        null,
        fetchedQuery.getQuery(),
        fetchedQuery.getGoogleJobId(),
        fetchedQuery.getProjectId(),
        fetchedQuery.getDefaultDataset(),
        fetchedQuery.isUsingMaterializedView(),
        fetchedQuery.isUsingCache(),
        fetchedQuery.getDate(),
        fetchedQuery.getStatistics());
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
}
