package com.achilio.mvm.service;

import static com.achilio.mvm.service.OptimizerStrategyType.SORT_BY_QUERY_COST;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SortByQueryCostOptimizerTest {

  private final FieldSet fieldSet1 = mock(FieldSet.class);
  private final FieldSet fieldSet2 = mock(FieldSet.class);
  private final FieldSet fieldSet3 = mock(FieldSet.class);
  private final FieldSet fieldSet4 = mock(FieldSet.class);
  private final FieldSet fieldSet5 = mock(FieldSet.class);

  @Test
  public void simple() {
    Optimizer optimizer = OptimizerFactory.createOptimizer(SORT_BY_QUERY_COST, 3);
    List<FieldSet> expected = Arrays.asList(fieldSet1, fieldSet2, fieldSet3);
    List<FieldSet> actual = optimizer.optimize(
        Arrays.asList(fieldSet3, fieldSet5, fieldSet1, fieldSet2, fieldSet4));
    assertEquals(expected.size(), actual.size());
    assertThat(expected).hasSameElementsAs(actual);
  }

  @Before
  public void setUp() {
    when(fieldSet1.cost()).thenReturn(1000L);
    when(fieldSet2.cost()).thenReturn(800L);
    when(fieldSet3.cost()).thenReturn(700L);
    when(fieldSet4.cost()).thenReturn(100L);
    when(fieldSet5.cost()).thenReturn(50L);
  }

}
