package com.achilio.mvm.service.entities;

import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import java.util.Date;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

/**
 * Represent a BigQueryJob as achilio-compatible query. Throw an exception if the job is not an
 * instance of QueryConfigurationJob
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("bigquery_job")
public class BigQueryJob extends Query {

  public BigQueryJob(Job job) {
    this();
    throwExceptionIfNotQueryJob(job);
    final QueryJobConfiguration configuration = job.getConfiguration();
    final JobId jobId = job.getJobId();
    final DatasetId datasetId = configuration.getDefaultDataset();
    final BigQueryError error = job.getStatus().getError();
    setQuery(configuration.getQuery());
    setProjectId(jobId.getProject());
    setStatistics(job);
    setId(jobId.getJob());
    setDefaultDataset(datasetId == null ? null : datasetId.getDataset());
    setError(error != null ? error.getMessage() : Strings.EMPTY);
  }

  private void setStatistics(Job job) {
    QueryStatistics stats = job.getStatistics();
    if (stats != null) {
      setStartTime(new Date(stats.getStartTime()));
      setProcessedBytes(
          stats.getTotalBytesProcessed() == null ? 0L : stats.getTotalBytesProcessed());
      setBilledBytes(stats.getTotalBytesBilled() == null ? 0L : stats.getTotalBytesBilled());
      setUseCache(stats.getCacheHit() != null && stats.getCacheHit());
      setUseMaterializedView(containsManagedMVUsageInQueryStages(stats.getQueryPlan()));
    }
  }

  public boolean containsManagedMVUsageInQueryStages(List<QueryStage> stages) {
    return stages != null
        && stages.stream()
            .flatMap(s -> s.getSteps().stream())
            .anyMatch(this::containsSubStepUsingMVM);
  }

  public boolean containsSubStepUsingMVM(QueryStep step) {
    return step.getSubsteps().stream()
        .anyMatch(subStep -> subStep.contains("SELECT") && (subStep.contains("achilio_mv_")));
  }

  private void throwExceptionIfNotQueryJob(Job job) {
    if (!(job.getConfiguration() instanceof QueryJobConfiguration)) {
      throw new IllegalArgumentException("BigQueryJob must be a QueryJob");
    }
  }
}
