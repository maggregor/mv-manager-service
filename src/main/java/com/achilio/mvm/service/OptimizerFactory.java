package com.achilio.mvm.service;

public class OptimizerFactory {

  public static Optimizer createOptimizer() {
    return new BruteForceOptimizer(20);
  }
}
