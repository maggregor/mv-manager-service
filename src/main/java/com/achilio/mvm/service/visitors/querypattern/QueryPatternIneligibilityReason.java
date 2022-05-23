package com.achilio.mvm.service.visitors.querypattern;

public enum QueryPatternIneligibilityReason {
  DOES_NOT_CONTAIN_A_GROUP_BY("This query require a GROUP BY"),
  CONTAINS_UNSUPPORTED_JOIN("The query is using a unsupported JOIN for Materialized Views");

  private final String details;

  QueryPatternIneligibilityReason(String details) {
    this.details = details;
  }

  public String getDetails() {
    return details;
  }
}
