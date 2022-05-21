package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.repositories.FieldRepository;
import com.achilio.mvm.service.repositories.QueryPatternRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IntegrationQueryPatternRepositoryTest {

  private static final String PROJECT_ID = "project";

  private static final String DATASET_NAME = "dataset";
  private static final String TABLE_REF1 = "project.dataset.table1";
  private static final String COL_NAME1 = "col1";
  private static final String COL_NAME2 = "col2";
  private static final FieldType FIELD_TYPE1 = FieldType.AGGREGATE;

  @Autowired
  private FieldRepository fieldRepository;
  @Autowired
  private QueryPatternRepository queryPatternRepository;

  @Before
  public void setup() {
    Field field1 = new Field(FIELD_TYPE1, COL_NAME1);
    Field field2 = new Field(FIELD_TYPE1, COL_NAME2);
    Field field3 = new Field(FIELD_TYPE1, COL_NAME2);

    QueryPattern queryPattern1 = new QueryPattern();
    QueryPattern queryPattern2 = new QueryPattern();
    queryPattern1.setProjectId(PROJECT_ID);
    queryPattern2.setProjectId(PROJECT_ID);
    queryPattern1.add(field1);
    queryPattern1.add(field2);
    queryPattern1.add(field3);
    queryPattern2.add(field1);
    queryPatternRepository.save(queryPattern1);
    queryPatternRepository.save(queryPattern2);
  }

  @Test
  public void simpleTest() {
    List<QueryPattern> queryPatterns = queryPatternRepository.findAll();
    assertNotNull(queryPatterns.get(0).getId());
    assertNotNull(queryPatterns.get(1).getId());
    assertEquals(2, queryPatterns.size());
    assertEquals(2, queryPatterns.get(0).getFields().size());
    assertEquals(1, queryPatterns.get(1).getFields().size());
  }
}
