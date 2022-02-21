package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.achilio.mvm.service.visitors.FieldSetMerger;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class BruteForceOptimizerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(BruteForceOptimizerTest.class);

  private final List<Field> AVAILABLE_FIELDS =
      Arrays.asList(
          new ReferenceField("f1", "f1", 10),
          new ReferenceField("f2", "f2", 9),
          new ReferenceField("f3", "f3", 8),
          new ReferenceField("f4", "f4", 7),
          new ReferenceField("f5", "f5", 6),
          new ReferenceField("f6", "f6", 5),
          new ReferenceField("f7", "f7", 4),
          new ReferenceField("f8", "f8", 3),
          new ReferenceField("f9", "f9", 2),
          new ReferenceField("f10", "f10", 1),
          new ReferenceField("f11", "f11", 1),
          new ReferenceField("f12", "f12", 2),
          new ReferenceField("f13", "f13", 3));

  // List of unmerged(?) queries (fieldset)
  // Best weight should be: MV1:avgWeight(10 * 8) * 3 + MV2:avgWeight(10 * 9) * 2
  // 8 * 9 / 2 = 36
  // (8 * 10) / 2 => 40 averageCol MV1 soit weight = 40 colAverage * 3 totalHits = 120
  // 9 * 10 / 2 = 45 avgCol MV2 soit weight = 45 colAverage * 2 totalHits = 90
  // Best weight is 210
  private final List<FieldSet> TEST_FIELDSET1 =
      Arrays.asList(fieldSet(0), fieldSet(0, 1), fieldSet(0, 2));

  // This should be [0(3hits), 1(1hit), 2(1hit)]
  private final List<FieldSet> TEST_MERGED_FIELDSET1 =
      FieldSetMerger.mergeSameFieldSets(TEST_FIELDSET1);

  @Test
  public void simpleTest1() {
    BruteForceOptimizer optimizer = new BruteForceOptimizer(2, 100);
    List<FieldSet> fieldSet = optimizer.optimize(TEST_MERGED_FIELDSET1);
    for (int i = 0; i < fieldSet.size(); i++) {
      FieldSet f = fieldSet.get(i);
      LOGGER.info(
          "MV{} -> {} - Weight={} ColAverage={}",
          i + 1,
          f.toString(),
          optimizer.getWeight(f),
          optimizer.getFieldWeightAverage(f));
    }
    assertEquals(210L, optimizer.getTotalWeight(fieldSet));
  }

  /**
   * Generation
   *
   * <p>F1, F2, F3
   *
   * <p>MV1: MV2: MV3:
   */
  @Test
  public void simpleTest() {
    List<FieldSet> generatedFieldSet = generateRandomFieldSets(500);
    BruteForceOptimizer optimizer = new BruteForceOptimizer(20, 1_000_000_000);
    List<FieldSet> fieldSet = optimizer.optimize(generatedFieldSet);
    for (int i = 0; i < fieldSet.size(); i++) {
      FieldSet f = fieldSet.get(i);
      LOGGER.info(
          "MV{} -> {} - Weight={} ColAverage={}",
          i + 1,
          f.toString(),
          optimizer.getWeight(f),
          optimizer.getFieldWeightAverage(f));
    }
  }

  public List<FieldSet> generateRandomFieldSets(int numberFieldSets) {
    List<FieldSet> fieldSets = new ArrayList<>();
    for (int i = 0; i < numberFieldSets; i++) {
      fieldSets.add(generateRandomFieldSet());
    }
    LOGGER.info("Generated {} random field sets", fieldSets.size());
    fieldSets = FieldSetMerger.merge(fieldSets);
    LOGGER.info("Merged: {} uniq field sets", fieldSets.size());
    return fieldSets;
  }

  public FieldSet generateRandomFieldSet() {
    FieldSet fieldSet = new DefaultFieldSet();
    int numberOfFields = RandomUtils.nextInt(1, AVAILABLE_FIELDS.size() + 1);

    /** MV1: col1, col2, coL3 MV2: col2, col3, col4, col5 MV3: col3 */
    while (fieldSet.fields().size() < numberOfFields) {
      Field field = AVAILABLE_FIELDS.get(RandomUtils.nextInt(0, AVAILABLE_FIELDS.size()));
      if (!fieldSet.fields().contains(field)) {
        fieldSet.add(field);
      }
    }
    return fieldSet;
  }

  public FieldSet fieldSet(int... index) {
    Set<Field> fields = new HashSet<>();
    for (int i : index) {
      if (i < 0 || AVAILABLE_FIELDS.size() - 1 < i) {
        throw new IllegalArgumentException("Field index doesn't exists");
      }
      fields.add(AVAILABLE_FIELDS.get(i));
    }
    return new DefaultFieldSet(fields);
  }

  @Test
  public void onGenereTout() {
    int combinations = 0;
    /*for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        for (int k = 0; k < 4; k++) {
          for (int l = 0; l < 4; l++) {
            for (int m = 0; m < 4; m++) {
              for (int n = 0; n < 4; n++) {
                combinations++;
              }
            }
          }
        }
      }
    }*/
    int r = 100;
    int nn = 4;
    BigInteger p = BigInteger.valueOf(1);
    for (int i = 0; i < r; i++) {
      p = p.multiply(BigInteger.valueOf(nn));
    }
    for (BigInteger i = BigInteger.valueOf(0);
        i.compareTo(p) == -1;
        i = i.add(BigInteger.valueOf(1))) {
      BigInteger t = i;
      String comb = "(";
      for (int j = 0; j < r; j++) {
        comb = comb + String.format("%2d, ", t.longValue() % nn);
        t = t.divide(BigInteger.valueOf(nn));
      }
      comb = comb.substring(0, comb.length() - 2) + ')';
      combinations++;
    }
    System.err.println(combinations + " <---- LE NOMBRE DE COMBINATION");
  }
}
