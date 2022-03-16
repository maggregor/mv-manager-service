package com.achilio.mvm.service.visitors;

public enum FieldSetIneligibilityReason {
  DOES_NOT_CONTAIN_A_GROUP_BY("This query require a GROUP BY"),
  CONTAINS_UNSUPPORTED_JOIN("The query is using a unsupported JOIN for Materialized Views");

  private final String details;

  FieldSetIneligibilityReason(String details) {
    this.details = details;
  }

  public String getDetails() {
    return details;
  }
}
