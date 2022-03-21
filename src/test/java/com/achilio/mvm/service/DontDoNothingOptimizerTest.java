package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DontDoNothingOptimizerTest {

  @Test
  public void simple() {
    Optimizer optimizer;
    // Initialize field sets
    List<FieldSet> fieldSets = new ArrayList<>();
    fieldSets.add(FieldSetHelper.createFieldSet(new ReferenceField("col1")));
    fieldSets.add(FieldSetHelper.createFieldSet(new FunctionField("col2")));
    // Remove exceeded
    optimizer = OptimizerFactory.createOptimizer(OptimizerStrategyType.DONT_DO_NOTHING);
    List<FieldSet> actual = optimizer.optimize(fieldSets);
    assertEquals(fieldSets.size(), actual.size());
    assertEquals(fieldSets, actual);
  }
}
