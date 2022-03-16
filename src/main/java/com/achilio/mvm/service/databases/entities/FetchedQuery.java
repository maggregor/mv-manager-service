package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.ATableId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public class FetchedQuery {

  private final String query;
  private final String projectId;
  private boolean useMaterializedView;
  private boolean useCache;
  private LocalDate startTime;
  private boolean canUseMaterializedViews;
  // Discovered tables in the SQL query
  @Deprecated private Set<FetchedTable> refTables;
  // Discovered tables ids in the SQL statement
  private Set<ATableId> tables;
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

  public Set<ATableId> getTables() {
    return this.tables;
  }

  public void setTables(Set<ATableId> tables) {
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

  public void setStartTime(Long startTime) {
    this.startTime = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public LocalDate getDate() {
    return this.startTime;
  }

  public String getProjectId() {
    return this.projectId;
  }

  public void setCanUseMaterializedViews(boolean canUseMaterializedViews) {
    this.canUseMaterializedViews = canUseMaterializedViews;
  }

  public boolean canUseMaterializedViews() {
    return canUseMaterializedViews;
  }
}
