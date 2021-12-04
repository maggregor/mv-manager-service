package com.achilio.mvm.service;

import static com.achilio.mvm.service.OptimizerStrategyType.BRUTE_FORCE;

public class OptimizerFactory {

  private static final OptimizerStrategyType DEFAULT_STRATEGY_TYPE = BRUTE_FORCE;

  private OptimizerFactory() {
  }

  public static Optimizer createOptimizerWithDefaultStrategy(int maxFieldSet) {
    return createOptimizer(DEFAULT_STRATEGY_TYPE, maxFieldSet);
  }

  public static Optimizer createOptimizer(OptimizerStrategyType strategyType, int maxFieldSet) {
    switch (strategyType) {
      case SIMPLE_MERGE_BY_CARDINALITY:
        return new SimpleCardinalityMergeOptimizer(maxFieldSet);
      case BRUTE_FORCE:
        return new BruteForceOptimizer(maxFieldSet);
    }
    throw new IllegalArgumentException("Factory doesn't support this strategy: " + strategyType);
  }
  
}
