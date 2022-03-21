package com.achilio.mvm.service.databases.entities;

import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.commons.lang3.StringUtils;

/**
 * Represent a fetched query from data warehouse fetching process.
 *
 * @see DatabaseFetcher
 */
public class FetchedQuery {

  private final String query;
  private final String projectId;
  private String googleJobId;
  private boolean useMaterializedView;
  private boolean useCache;
  private LocalDate startTime;
  private boolean canUseMaterializedViews;
  private QueryUsageStatistics statistics;
  private String defaultDataset;

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

  public String getGoogleJobId() {
    return googleJobId;
  }

  public void setGoogleJobId(String googleJobId) {
    this.googleJobId = googleJobId;
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

  public boolean hasDefaultDataset() {
    return StringUtils.isNotEmpty(defaultDataset);
  }

  public String getDefaultDataset() {
    return defaultDataset;
  }

  public void setDefaultDataset(String defaultDataset) {
    this.defaultDataset = defaultDataset;
  }
}
