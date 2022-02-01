package com.achilio.mvm.service;

public enum OptimizerStrategyType {

  /**
   * Returns the extracted fieldset from the query.
   */
  DONT_DO_NOTHING,
  /**
   * Returns the extracted fieldset from the query.
   */
  SORT_BY_QUERY_COST,
  /**
   * Merge fieldset by count distinct.
   */
  MERGING_BY_COUNT_DISTINCT;

}
