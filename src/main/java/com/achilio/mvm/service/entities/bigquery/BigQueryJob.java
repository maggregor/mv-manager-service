package com.achilio.mvm.service.entities.bigquery;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.AQueryStatistics;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobConfiguration;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.common.base.Preconditions;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@DiscriminatorValue("bigquery")
public class BigQueryJob extends AQuery {

  @Enumerated(EnumType.STRING)
  @Column
  private JobConfiguration.Type bigQueryJobType;

  public BigQueryJob(Job job) {
    this();
    final QueryJobConfiguration configuration = job.getConfiguration();
    final JobId jobId = job.getJobId();
    final DatasetId datasetId = configuration.getDefaultDataset();
    final BigQueryError error = job.getStatus().getError();
    setQuery(configuration.getQuery());
    setProjectId(jobId.getProject());
    setQueryStatistics(getStatisticsAdapter(job));
    setId(jobId.getJob());
    setDefaultDataset(datasetId == null ? null : datasetId.getDataset());
    setError(error != null ? error.getMessage() : Strings.EMPTY);
    setUser(job.getUserEmail());
    setBigQueryJobType(job.getConfiguration().getType());
  }

  private AQueryStatistics getStatisticsAdapter(Job job) {
    Preconditions.checkNotNull(job.getStatistics(), "Statistics cannot be null");
    JobStatistics stats = job.getStatistics();
    // Time
    setStartTime(stats.getStartTime() == null ? null : new Date(stats.getStartTime()));
    setEndTime(stats.getEndTime() == null ? null : new Date(stats.getEndTime()));
    switch (job.getConfiguration().getType()) {
      case QUERY:
        return new BigQueryQueryStatistics(stats, super.getProjectId());
      case LOAD:
        return new BigQueryLoadStatistics(stats);
      case COPY:
        return new BigQueryCopyStatistics(stats);
      case EXTRACT:
        return new BigQueryExtractStatistics(stats);
    }
    return null;
  }

}
