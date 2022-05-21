package com.achilio.mvm.service.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.repositories.FieldRepository;
import com.achilio.mvm.service.repositories.QueryPatternRepository;
import com.achilio.mvm.service.services.QueryPatternService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class QueryPatternController {

  private static final String MOCK_PROJECT_ID = "mockProject";
  private final QueryPatternService service;
  @Autowired
  QueryPatternRepository queryPatternRepository;
  @Autowired
  FieldRepository fieldRepository;

  public QueryPatternController(QueryPatternService service) {
    this.service = service;
  }

  @GetMapping(path = "/mock/query-pattern", produces = APPLICATION_JSON_VALUE)
  public List<QueryPattern> mockGetAllQPatterns() {
    return service.getAllQueryPatterns(MOCK_PROJECT_ID);
  }

  @GetMapping(path = "/mock/field", produces = APPLICATION_JSON_VALUE)
  public List<Field> mockGetAllFields() {
    return null;//service.getAllFields(MOCK_PROJECT_ID);
  }

  @PostMapping(path = "/mock/query-pattern", produces = APPLICATION_JSON_VALUE)
  public void mockCreateAllTestPatterns() {
    service.deleteAllQueryPatterns(MOCK_PROJECT_ID);
    //service.deleteAllFields(MOCK_PROJECT_ID);
    Field field1 =
        new Field(
            FieldType.AGGREGATE, "col1");
    Field field2 =
        new Field(
            FieldType.AGGREGATE, "col2");
    QueryPattern queryPattern1 = new QueryPattern();
    QueryPattern queryPattern2 = new QueryPattern();
    queryPattern1.add(field1);
    queryPattern1.add(field2);
    queryPattern2.add(field1);
    queryPatternRepository.save(queryPattern1);
    queryPatternRepository.save(queryPattern2);
  }
}
