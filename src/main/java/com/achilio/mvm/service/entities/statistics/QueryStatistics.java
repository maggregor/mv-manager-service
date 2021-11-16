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
  private int queries = 0;
  private long billedBytes = 0;
  private long processedBytes = 0;
  private MutableInt eligibles = new MutableInt();
  private MutableInt ineligibles = new MutableInt();
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
        incrementEligibles();
      } else {
        incrementIneligibles();
      }
    }
  }

  private void incrementIneligibles() {
    this.ineligibles.increment();
  }

  private void incrementEligibles() {
    this.eligibles.increment();
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
    ineligibleReasons.forEach((k, v) ->
        this.ineligibleReasons.merge(k, v, (v1, v2) -> new MutableInt(v1.addAndGet(v2))));
  }

  public int getQueries() {
    return queries;
  }

  public long getBilledBytes() {
    return billedBytes;
  }

  public long getProcessedBytes() {
    return processedBytes;
  }

  public void incrementQueries() {
    this.queries += 1;
  }

  public int getEligibles() {
    return this.eligibles.getValue();
  }

  public int getIneligibles() {
    return this.ineligibles.getValue();
  }

  public void addProcessedBytes(long processedBytes) {
    this.processedBytes += processedBytes;
  }

  public void addBilledBytes(long billedBytes) {
    this.billedBytes += billedBytes;
  }

  public void addQueries(int queries) {
    this.queries += queries;
  }

  public void addEligibles(int eligibles) {
    this.eligibles.add(eligibles);
  }

  public void addIneligibles(int ineligibles) {
    this.ineligibles.add(ineligibles);
  }
}
