package com.achilio.mvm.service;

public class OptimizerFactory {
  
  public static Optimizer createOptimizer(int maxFieldSet) {
    return new BruteForceOptimizer(maxFieldSet);
  }
}
