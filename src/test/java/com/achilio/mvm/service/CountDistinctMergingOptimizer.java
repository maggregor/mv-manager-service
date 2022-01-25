package com.achilio.mvm.service;

import static com.achilio.mvm.service.OptimizerStrategyType.MERGING_BY_COUNT_DISTINCT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CountDistinctMergingOptimizer {

  private Optimizer optimizer;
  private Field mockField100;
  private Field mockField75;
  private Field mockField50;
  private Field mockField25;
  private Field mockField1;

  @Test
  public void simple() {
    FieldSet mockFieldset = mock(FieldSet.class);
    when(mockFieldset.fields()).thenReturn(Sets.newSet(mockField1, mockField25));
    when(mockFieldset.fields()).thenReturn(Sets.newSet(mockField1, mockField25));
  }

  @Before
  public void setUp() {
    optimizer = OptimizerFactory.createOptimizer(MERGING_BY_COUNT_DISTINCT, 1);
    mockField100 = createMockField("col_a", 100F);
    mockField75 = createMockField("col_b", 75F);
    mockField50 = createMockField("col_c", 50F);
    mockField25 = createMockField("col_d", 25F);
    mockField1 = createMockField("col_e", 1F);
  }

  private Field createMockField(String name, float distinctValuePercent) {
    Field mockField = mock(Field.class);
    when(mockField.name()).thenReturn(name);
    when(mockField.getDistinctValuePercent()).thenReturn(distinctValuePercent);
    return mockField;
  }
}
