package com.achilio.mvm.service;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Arrays;
import java.util.Comparator;
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
    mockFieldSet1 = new DefaultFieldSet(Sets.newSet(fieldA, fieldB));
    mockFieldSet1Copy = new DefaultFieldSet(Sets.newSet(fieldA, fieldB));
    mockFieldSet2 = new DefaultFieldSet(Sets.newSet(fieldC));
  }

  @Test
  public void simpleMerge() {
    List<FieldSet> sortedMerge =
        FieldSetMerger.mergeSame(Arrays.asList(mockFieldSet1, mockFieldSet1Copy, mockFieldSet2))
            .stream()
            .sorted(Comparator.comparingInt(FieldSet::getHits))
            .collect(toList());
    assertEquals(2, sortedMerge.size());
    assertEquals(1, sortedMerge.get(0).getHits());
    assertEquals(4, sortedMerge.get(1).getHits());
  }
}
