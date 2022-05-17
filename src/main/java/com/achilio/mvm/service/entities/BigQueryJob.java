package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
public class BigQueryJob extends AQuery {

  // Match "FROM x" and GROUP "x"
  private static Pattern QUERY_PLAN_TABLE_PATTERN = Pattern.compile(
      "(?ims)\\b(?:FROM)\\s+(\\w+(?:.\\w+)*)", Pattern.CASE_INSENSITIVE);

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
    if (job.getStatistics() != null && job.getStatistics() instanceof QueryStatistics) {
      List<QueryStage> stages = ((QueryStatistics) job.getStatistics()).getQueryPlan();
      setJobTableId(findTableIdRead(stages));
    }
  }

  private void setStatistics(Job job) {
    QueryStatistics stats = job.getStatistics();
    if (stats != null) {
      setStartTime(stats.getStartTime() == null ? null : new Date(stats.getStartTime()));
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
        .anyMatch(subStep -> subStep.contains("FROM") && (subStep.contains(
            MaterializedView.MV_NAME_PREFIX)));
  }

  private void throwExceptionIfNotQueryJob(Job job) {
    if (!(job.getConfiguration() instanceof QueryJobConfiguration)) {
      throw new IllegalArgumentException("BigQueryJob must be a QueryJob");
    }
  }

  /**
   * Go through the tree of QueryStage -> QueryStep -> SubSteps and extract all the tables found
   *
   * @param queryPlan
   * @return
   */
  private List<String> findTableIdRead(List<QueryStage> queryPlan) {
    return queryPlan.stream()
        .flatMap(q -> q.getSteps().stream().flatMap(
            step -> step.getSubsteps().stream().map(this::extractTableIdFromQuerySubStepRegex)))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Find table ids in sub steps with the dedicated regex. If a table id found doesn't have a
   * project id but just dataset name and table name this method add the project id of the current
   * job to the table id.
   *
   * @param subStep
   * @return
   */
  private String extractTableIdFromQuerySubStepRegex(String subStep) {
    Matcher matcher = QUERY_PLAN_TABLE_PATTERN.matcher(subStep);
    if (matcher.find()) {
      final String stringTableId = matcher.group(1);
      ATableId tableId = ATableId.parse(stringTableId);
      if (tableId != null && isValidTableId(stringTableId)) {
        if (tableId.getProjectId() == null) {
          // Add project id if not found in the query plan
          tableId.setProjectId(getProjectId());
        }
        return tableId.getTableId();
      }
    }
    return null;
  }

  /**
   * Filter on some not-table-
   *
   * @param tableId
   * @return
   */
  private boolean isValidTableId(String tableId) {
    return !tableId.endsWith("_mvdelta") && !tableId.contains("_mvdelta__");
  }
}
