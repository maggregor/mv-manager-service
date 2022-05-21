package com.achilio.mvm.service.entities.bigquery;

import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.visitors.ATableId;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.JobStatistics.QueryStatistics;
import com.google.cloud.bigquery.QueryStage;
import com.google.cloud.bigquery.QueryStage.QueryStep;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
public class BigQueryQueryStatistics extends BigQueryJobStatistics {

  // Match "FROM x" and GROUP "x"
  private static final Pattern QUERY_PLAN_TABLE_PATTERN = Pattern.compile(
      "(?ims)\\b(?:FROM)\\s+(\\w+(?:.\\w+)*)", Pattern.CASE_INSENSITIVE);

  private final boolean useMaterializedView;

  private final String defaultProjectId;

  @ElementCollection
  @CollectionTable(
      name = "bigquery_query_plan_table_ids",
      joinColumns = @JoinColumn(name = "query_id", referencedColumnName = "id"))
  @Column(name = "table_id")
  @JoinColumn(name = "query_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private final List<String> queryPlanTableIds;

  private final Long billedBytes;
  private final Long totalBytesProcessed;

  public BigQueryQueryStatistics(JobStatistics s, String defaultProjectId) {
    super(s);
    this.defaultProjectId = defaultProjectId;
    QueryStatistics stats = (QueryStatistics) s;
    super.setCached(Optional.ofNullable(stats.getCacheHit()).orElse(false));
    this.useMaterializedView = isUsingMVByAchilio(stats.getQueryPlan());
    this.queryPlanTableIds = findTableIdRead(stats.getQueryPlan());
    this.totalBytesProcessed = stats.getTotalBytesProcessed();
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

  private boolean isUsingMVByAchilio(List<QueryStage> stages) {
    return stages != null
        && stages.stream()
        .flatMap(s -> s.getSteps().stream())
        .anyMatch(this::containsSubStepUsingMVM);
  }

  private boolean containsSubStepUsingMVM(QueryStep step) {
    return step.getSubsteps().stream()
        .anyMatch(subStep -> subStep.contains("FROM") && (subStep.contains(
            MaterializedView.MV_NAME_PREFIX)));
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
          tableId.setProjectId(defaultProjectId);
        }
        return tableId.getTableId();
      }
    }
    return null;
  }

  private boolean isValidTableId(String tableId) {
    return !tableId.endsWith("_mvdelta") && !tableId.contains("_mvdelta__");
  }

}
