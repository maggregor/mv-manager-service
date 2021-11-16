package com.achilio.mvm.service.entities.statistics;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;

public class QueryStatistics {

  private final boolean enableComputeIneligibilityReasons;
  private int totalQueries = 0;
  private long totalBilledBytes = 0;
  private long totalProcessedBytes = 0;
  private MutableInt eligible = new MutableInt();
  private MutableInt ineligible = new MutableInt();

  @JsonProperty("ineligibleReasons")
  private Map<QueryIneligibilityReason, MutableInt> ineligibleReasons = new HashMap<>();

  public QueryStatistics() {
    this(false);
  }

  public QueryStatistics(boolean computeIneligibilityReasons) {
    this(Collections.emptyList(), computeIneligibilityReasons);
  }

  public QueryStatistics(List<FetchedQuery> queries) {
    this(queries, false);
  }

  public QueryStatistics(List<FetchedQuery> queries, boolean enableComputeIneligibilityReasons) {
    this.enableComputeIneligibilityReasons = enableComputeIneligibilityReasons;
    if (enableComputeIneligibilityReasons) {
      ineligibleReasons = defaultReasonStatistics();
    }
    queries.forEach(this::addQuery);
  }

  private Map<QueryIneligibilityReason, MutableInt> defaultReasonStatistics() {
    Map<QueryIneligibilityReason, MutableInt> mutableIntMap = new HashMap<>();
    for (QueryIneligibilityReason reason : QueryIneligibilityReason.values()) {
      mutableIntMap.put(reason, new MutableInt());
    }
    return mutableIntMap;
  }

  private void addQuery(FetchedQuery query) {
    incrementQueries();
    addBilledBytes(query.getStatistics().getBilledBytes());
    addProcessedBytes(query.getStatistics().getProcessedBytes());
    if (enableComputeIneligibilityReasons) {
      incrementQueryIneligibilityReasonIfEnabled(query);
      if (query.isEligible()) {
        incrementEligible();
      } else {
        incrementIneligibles();
      }
    }
  }

  private void incrementIneligibles() {
    this.ineligible.increment();
  }

  private void incrementEligible() {
    this.eligible.increment();
  }

  public void incrementQueryIneligibilityReasonIfEnabled(FetchedQuery query) {
    if (ineligibleReasons != null) {
      query.getQueryIneligibilityReasons().forEach(r -> ineligibleReasons.get(r).increment());
    }
  }

  public Map<QueryIneligibilityReason, MutableInt> getIneligibleReasons() {
    return this.ineligibleReasons;
  }

  public void addIneligibleReasons(Map<QueryIneligibilityReason, MutableInt> ineligibleReasons) {
    ineligibleReasons.forEach(
        (k, v) -> this.ineligibleReasons.merge(k, v, (v1, v2) -> new MutableInt(v1.addAndGet(v2))));
  }

  public int getTotalQueries() {
    return totalQueries;
  }

  public long getTotalBilledBytes() {
    return totalBilledBytes;
  }

  public long getTotalProcessedBytes() {
    return totalProcessedBytes;
  }

  public void incrementQueries() {
    this.totalQueries += 1;
  }

  public int getEligible() {
    return this.enableComputeIneligibilityReasons == false ? -1 : this.eligible.getValue();
  }

  public int getIneligible() {
    return this.enableComputeIneligibilityReasons == false ? -1 : this.ineligible.getValue();
  }

  public void addProcessedBytes(long processedBytes) {
    this.totalProcessedBytes += processedBytes;
  }

  public void addBilledBytes(long billedBytes) {
    this.totalBilledBytes += billedBytes;
  }

  public void addQueries(int queries) {
    this.totalQueries += queries;
  }

  public void addEligible(int eligible) {
    this.eligible.add(eligible);
  }

  public void addIneligibles(int ineligibles) {
    this.ineligible.add(ineligibles);
  }
}
