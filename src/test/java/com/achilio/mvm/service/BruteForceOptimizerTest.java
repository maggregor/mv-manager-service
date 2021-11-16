package com.achilio.mvm.service;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class BruteForceOptimizerTest {

  @Test
  public void simpleReachLimitFieldSet() {
    Optimizer optimizer;
    // Initialize field sets
    Set<FieldSet> fieldSets = new LinkedHashSet<>();
    fieldSets.add(FieldSetHelper.createFieldSet(new ReferenceField("col1")));
    fieldSets.add(FieldSetHelper.createFieldSet(new FunctionField("col2")));
    // Remove exceeded
    optimizer = OptimizerFactory.createOptimizer(1);
    Assert.assertEquals(1, optimizer.optimize(fieldSets).size());
  }
}
