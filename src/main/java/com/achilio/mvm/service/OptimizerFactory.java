package com.achilio.mvm.service;

import static com.achilio.mvm.service.OptimizerStrategyType.SORT_BY_QUERY_COST;

public class OptimizerFactory {

  private static final OptimizerStrategyType DEFAULT_STRATEGY_TYPE = SORT_BY_QUERY_COST;

  private OptimizerFactory() {
  }

  public static Optimizer createOptimizerWithDefaultStrategy(int maxFieldSet) {
    return createOptimizer(DEFAULT_STRATEGY_TYPE, maxFieldSet);
  }

  public static Optimizer createOptimizer(OptimizerStrategyType strategyType, int maxFieldSet) {
    switch (strategyType) {
      case MERGING_BY_COUNT_DISTINCT:
        return new CountDistinctMergingOptimizer();
      case DONT_DO_NOTHING:
        return new DontDoNothingOptimizer(maxFieldSet);
      case SORT_BY_QUERY_COST:
        return new SortByQueryCostOptimizer(maxFieldSet);
    }
    throw new IllegalArgumentException("Factory doesn't support this strategy: " + strategyType);
  }

}
