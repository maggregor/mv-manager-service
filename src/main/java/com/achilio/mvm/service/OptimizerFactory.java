package com.achilio.mvm.service;

public class OptimizerFactory {

  private static final OptimizerStrategyType DEFAULT_STRATEGY_TYPE = OptimizerStrategyType.DONT_DO_NOTHING;

  private OptimizerFactory() {
  }

  public static Optimizer createOptimizerWithDefaultStrategy() {
    return createOptimizer(DEFAULT_STRATEGY_TYPE);
  }

  public static Optimizer createOptimizer(OptimizerStrategyType strategyType) {
    switch (strategyType) {
      case MERGING_BY_COUNT_DISTINCT:
        return new CountDistinctMergingOptimizer();
      case DONT_DO_NOTHING:
        return new DontDoNothingOptimizer();
    }
    throw new IllegalArgumentException("Factory doesn't support this strategy: " + strategyType);
  }

}
