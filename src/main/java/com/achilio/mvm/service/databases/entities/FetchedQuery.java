package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.QueryEligible;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public class FetchedQuery implements QueryEligible {

  private final Set<QueryIneligibilityReason> reasons = new HashSet<>();
  private final String query;
  private boolean useMaterializedView;
  private boolean useCache;
  // Discovered tables in the SQL query
  private Set<FetchedTable> refTables;
  private QueryUsageStatistics statistics;

  public FetchedQuery(String query) {
    this.query = query;
  }

  public void setUseMaterializedView(boolean useMaterializedView) {
    this.useMaterializedView = useMaterializedView;
  }

  public boolean isUsingMaterializedView() {
    return this.useMaterializedView;
  }

  public Set<FetchedTable> getReferenceTables() {
    return this.refTables;
  }

  public void setReferenceTables(Set<FetchedTable> refTables) {
    this.refTables = refTables;
  }

  public QueryUsageStatistics getStatistics() {
    return this.statistics;
  }

  public void setStatistics(QueryUsageStatistics statistics) {
    this.statistics = statistics;
  }

  /**
   * The SQL statement of the fetched query.
   *
   * @return the SQL statement of the fetched query.
   */
  public String getQuery() {
    return this.query;
  }

  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }

  public boolean isUsingCache() {
    return this.useCache;
  }

  public void addQueryIneligibilityReason(QueryIneligibilityReason reason) {
    this.reasons.add(reason);
  }

  @Override
  public void removeQueryIneligibilityReason(QueryIneligibilityReason reason) {
    this.reasons.remove(reason);
  }

  @Override
  public void clearQueryIneligibilityReasons() {
    this.reasons.clear();
  }

  @Override
  public Set<QueryIneligibilityReason> getQueryIneligibilityReasons() {
    return this.reasons;
  }
}
