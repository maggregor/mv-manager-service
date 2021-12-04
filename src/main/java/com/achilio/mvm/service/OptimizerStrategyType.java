package com.achilio.mvm.service;

public enum OptimizerStrategyType {

  /**
   * Returns the extracted fieldset from the query.
   */
  BRUTE_FORCE,
  /**
   * Merge fieldset by cardinality.
   */
  SIMPLE_MERGE_BY_CARDINALITY;

}
