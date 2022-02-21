package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FieldSetMergerTest {

  private FieldSet mockFieldSet1;
  private FieldSet mockFieldSet1Copy;
  private FieldSet mockFieldSet2;

  @Before
  public void setUp() {
    Field fieldA = mock(Field.class);
    Field fieldB = mock(Field.class);
    Field fieldC = mock(Field.class);
    QueryUsageStatistics statistics = new QueryUsageStatistics();
    statistics.setProcessedBytes(100L);
    statistics.setBilledBytes(10L);
    mockFieldSet1 = new DefaultFieldSet(Sets.newSet(fieldA, fieldB));
    mockFieldSet1Copy = new DefaultFieldSet(Sets.newSet(fieldA, fieldB));
    mockFieldSet2 = new DefaultFieldSet(Sets.newSet(fieldC));
    mockFieldSet1.setStatistics(statistics);
    mockFieldSet1Copy.setStatistics(statistics);
    mockFieldSet2.setStatistics(statistics);
  }

  @Test
  public void simpleMerge() {
    final List<FieldSet> fieldSets = Arrays.asList(mockFieldSet1, mockFieldSet1Copy, mockFieldSet2);
    final List<FieldSet> expected = Arrays.asList(mockFieldSet1, mockFieldSet2);
    List<FieldSet> actual = FieldSetMerger.merge(fieldSets);
    assertThat(expected).hasSize(actual.size()).hasSameElementsAs(actual);
    assertThat(actual).hasSameElementsAs(expected);
  }

  @Test
  public void statisticsMerged() {
    final List<FieldSet> fieldSets = Arrays.asList(mockFieldSet1, mockFieldSet1Copy);
    List<FieldSet> actual = FieldSetMerger.merge(fieldSets);
    assertEquals(200L, actual.get(0).getStatistics().getProcessedBytes());
    assertEquals(20L, actual.get(0).getStatistics().getBilledBytes());
  }
}
