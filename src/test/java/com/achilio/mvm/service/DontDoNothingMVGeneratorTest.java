package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DontDoNothingMVGeneratorTest {

  @Test
  public void simple() {
    MVGenerator MVGenerator;
    // Initialize field sets
    List<QueryPattern> fieldSets = new ArrayList<>();
    fieldSets.add(QueryPatternHelper.createQueryPattern(new Field(FieldType.REFERENCE, "col1")));
    fieldSets.add(QueryPatternHelper.createQueryPattern(new Field(FieldType.AGGREGATE, "col2")));
    // Remove exceeded
    MVGenerator = MVFactory.createOptimizer(MVGeneratorStrategyType.DONT_DO_NOTHING);
    List<QueryPattern> actual = MVGenerator.generate(fieldSets);
    assertEquals(fieldSets.size(), actual.size());
    assertEquals(fieldSets, actual);
  }
}
