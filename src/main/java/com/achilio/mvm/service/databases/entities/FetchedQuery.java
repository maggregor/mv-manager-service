package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import com.achilio.mvm.service.visitors.TableId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public class FetchedQuery {

  private final Set<QueryIneligibilityReason> reasons = new HashSet<>();
  private final String query;
  private final String projectId;
  private boolean useMaterializedView;
  private boolean useCache;
  private LocalDate startTime;
  // Discovered tables in the SQL query
  @Deprecated private Set<FetchedTable> refTables;
  // Discovered tables ids in the SQL statement
  private Set<TableId> tables;
  private QueryUsageStatistics statistics;

  public FetchedQuery(String query) {
    this(null, query);
  }

  public FetchedQuery(String projectId, String query) {
    this.projectId = projectId;
    this.query = query;
  }

  public void setUseMaterializedView(boolean useMaterializedView) {
    this.useMaterializedView = useMaterializedView;
  }

  public boolean isUsingMaterializedView() {
    return this.useMaterializedView;
  }

  @Deprecated
  public Set<FetchedTable> getReferenceTables() {
    return this.refTables;
  }

  @Deprecated
  public void setReferenceTables(Set<FetchedTable> refTables) {
    this.refTables = refTables;
  }

  public Set<TableId> getTables() {
    return this.tables;
  }

  public void setTables(Set<TableId> tables) {
    this.tables = tables;
  }

  public QueryUsageStatistics getStatistics() {
    return this.statistics;
  }

  public void setStatistics(QueryUsageStatistics statistics) {
    this.statistics = statistics;
  }

  /** The SQL statement of the fetched query. */
  public String getQuery() {
    return this.query;
  }

  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }

  public boolean isUsingCache() {
    return this.useCache;
  }

  @Deprecated
  public void addQueryIneligibilityReason(QueryIneligibilityReason reason) {
    this.reasons.add(reason);
  }

  @Deprecated
  public void removeQueryIneligibilityReason(QueryIneligibilityReason reason) {
    this.reasons.remove(reason);
  }

  @Deprecated
  public void clearQueryIneligibilityReasons() {
    this.reasons.clear();
  }

  @Deprecated
  public Set<QueryIneligibilityReason> getQueryIneligibilityReasons() {
    return this.reasons;
  }

  public void setStartTime(Long startTime) {
    this.startTime = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public LocalDate getDate() {
    return this.startTime;
  }

  public String getProjectId() {
    return this.projectId;
  }
}
