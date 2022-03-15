package com.achilio.mvm.service.visitors;

public enum FieldSetIneligibilityReason {
  DOES_NOT_FILTER_OR_GROUP("The query doesn't not contains filtering or grouping clause"),
  CONTAINS_UNSUPPORTED_JOIN("The query is using a JOIN not supported in the MV");

  private String details;

  FieldSetIneligibilityReason(String details) {
    this.details = details;
  }

  public String getDetails() {
    return details;
  }
}
