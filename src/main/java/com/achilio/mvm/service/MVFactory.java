package com.achilio.mvm.service;

public class MVFactory {

  private static final MVGeneratorStrategyType DEFAULT_STRATEGY_TYPE =
      MVGeneratorStrategyType.DONT_DO_NOTHING;

  private MVFactory() {}

  public static MVGenerator createMVsWithDefaultStrategy() {
    return createOptimizer(DEFAULT_STRATEGY_TYPE);
  }

  public static MVGenerator createOptimizer(MVGeneratorStrategyType strategyType) {
    if (strategyType == MVGeneratorStrategyType.DONT_DO_NOTHING) {
      return new DontDoNothingMVGenerator();
    }
    throw new IllegalArgumentException("Factory doesn't support this strategy: " + strategyType);
  }
}
