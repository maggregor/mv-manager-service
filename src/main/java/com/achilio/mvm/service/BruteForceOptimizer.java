package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BruteForceOptimizer implements Optimizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BruteForceOptimizer.class);
  private final int expectedMaxFieldSet;
  private final List<FieldSet> currentSlots = new ArrayList<>();
  private List<FieldSet> lowestWeightSlots = new ArrayList<>();
  private long lowestWeight = Long.MAX_VALUE;
  private int totalRunCount;
  private boolean random;

  public BruteForceOptimizer(int expectedMaxFieldSet) {
    this(expectedMaxFieldSet, 1_000_000_000);
  }

  public BruteForceOptimizer(int expectedMaxFieldSet, int totalRunCount) {
    this.expectedMaxFieldSet = expectedMaxFieldSet;
    this.totalRunCount = totalRunCount;
  }

  public void run(List<FieldSet> fieldSets) {
    initializeCurrentSlots();
    randomMerge(fieldSets);
    finishRun();
  }

  private void randomMerge(List<FieldSet> fieldSets) {
    fieldSets.forEach(f -> currentSlots.get(nextRandomSlot()).merge(f));
  }

  private int nextRandomSlot() {
    return (int) (Math.random() * expectedMaxFieldSet);
  }

  private void finishRun() {
    long totalCurrentWeight = getTotalWeight(currentSlots);
    if (lowestWeight > totalCurrentWeight) {
      LOGGER.info("A lower total weight just found {}", totalCurrentWeight);
      lowestWeight = totalCurrentWeight;
      lowestWeightSlots = new ArrayList<>(currentSlots);
    }
  }

  public void initializeCurrentSlots() {
    this.currentSlots.clear();
    for (int i = 0; i < expectedMaxFieldSet; i++) {
      currentSlots.add(new DefaultFieldSet());
    }
  }

  @Override
  public List<FieldSet> optimize(List<FieldSet> fieldSet) {
    fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
    for (int currentRun = 0; currentRun < totalRunCount; currentRun++) {
      if (currentRun % (totalRunCount / 1000) == 0) {
        LOGGER.info("Run: " + currentRun + "/" + totalRunCount);
      }
      run(fieldSet);
    }
    lowestWeightSlots = FieldSetMerger.mergeSameFieldSets(lowestWeightSlots);
    LOGGER.info("End of optimization. The lowest total weight {}", lowestWeight);
    return lowestWeightSlots;
  }

  public long getSize(FieldSet fieldSet) {
    long size = 1;
    for (Field field : fieldSet.fields()) {
      size *= field.getCountDistinct();
    }
    return size;
  }

  public long getWeight(FieldSet fieldSet) {
    if (fieldSet.fields().isEmpty()) {
      // Empty field set
      return 0;
    }
    long fieldWeightAverage = getFieldWeightAverage(fieldSet);
    int totalHits = fieldSet.getTotalHits();
    return totalHits * fieldWeightAverage;
  }

  public long getFieldWeightAverage(FieldSet fieldSet) {
    long fieldSetSize = getSize(fieldSet);
    return fieldSetSize / fieldSet.fields().size();
  }

  public long getTotalWeight(List<FieldSet> fieldSets) {
    return fieldSets.stream().mapToLong(this::getWeight).sum();
  }
}
