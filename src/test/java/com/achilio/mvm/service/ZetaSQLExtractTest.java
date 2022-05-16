package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZetaSQLExtractTest {

  private final ZetaSQLExtract extract = new ZetaSQLExtract();

  @Test
  public void whenStatementOnlyHaveTable__thenReturnsFullTableId() {
    AQuery query = new AQuery("SELECT * FROM t", "myProject");
    query.setDefaultDataset("d");
    assertExtractTableId(query, ATableId.of("myProject", "d", "t"));
  }

  @Test
  public void whenStatementHaveBackTicks__thenReturnsFullTableId() {
    AQuery query = new AQuery("SELECT * FROM `myProject.d.t`", "myProject");
    assertExtractTableId(query, ATableId.of("myProject", "d", "t"));
  }

  @Test
  public void whenStatementOnlyHaveDatasetAndTable__thenReturnsFullTableId() {
    AQuery query = new AQuery("SELECT * FROM d.t", "myProject");
    assertExtractTableId(query, ATableId.of("myProject", "d", "t"));
  }

  @Test
  public void whenStatementHaveProjectDatasetName__thenReturnsFullTableId() {
    AQuery query;
    query = new AQuery("SELECT * FROM d.t", "myProject");
    assertExtractTableId(query, ATableId.of("myProject", "d", "t"));
    // With sub query
    query = new AQuery("SELECT * FROM ( SELECT * FROM t) q", "myProject");
    query.setDefaultDataset("d");
    assertExtractTableId(query, ATableId.of("myProject", "d", "t"));
    // With sub query
    query = new AQuery("SELECT * FROM ( SELECT * FROM t) q; SELECT 1 FROM t2 ", "myProject");
    query.setDefaultDataset("d");
    assertExtractTableId(
        query, ATableId.of("myProject", "d", "t"), ATableId.of("myProject", "d", "t2"));
  }

  @Test
  public void whenStatementDontHaveFromClause__thenReturnsEmptyATableIdList() {
    AQuery query;
    query = new AQuery("SELECT 100", "myProject");
    assertEmptyExtractTableId(query);
  }

  private void assertEmptyExtractTableId(AQuery query) {
    assertEquals(0, extract.extractATableIds(query).size());
  }

  private void assertExtractTableId(AQuery query, ATableId... expectedTableIds) {
    List<ATableId> actualTableIds = extract.extractATableIds(query);
    assertEquals(expectedTableIds.length, actualTableIds.size());
    assertEquals(Arrays.asList(expectedTableIds), actualTableIds);
  }
}
