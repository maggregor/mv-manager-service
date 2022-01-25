package com.achilio.mvm.service;

import static com.achilio.mvm.service.OptimizerStrategyType.DONT_DO_NOTHING;

public class OptimizerFactory {

  private static final OptimizerStrategyType DEFAULT_STRATEGY_TYPE = DONT_DO_NOTHING;

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
    }
    throw new IllegalArgumentException("Factory doesn't support this strategy: " + strategyType);
  }

}
