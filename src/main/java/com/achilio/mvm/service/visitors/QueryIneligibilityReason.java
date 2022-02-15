package com.achilio.mvm.service.visitors;

public enum QueryIneligibilityReason {
  /** Query parsing has failed */
  PARSING_FAILED,
  /** Query doesn't contains a clause with result filtering or aggregation. */
  DOES_NOT_FILTER_OR_AGGREGATE,
  /** The query contains more than 1 table and the JOIN is not supported. */
  MULTIPLE_TABLES_WITHOUT_SUPPORTED_JOIN,
  /** The query is using a JOIN not supported in the MV. */
  CONTAINS_UNSUPPORTED_JOIN_TYPE,
}
