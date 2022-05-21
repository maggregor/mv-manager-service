package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.repositories.FieldRepository;
import com.achilio.mvm.service.repositories.QueryPatternRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class QueryPatternService {

  private final QueryPatternRepository queryPatternRepository;
  private final FieldRepository fieldRepository;

  public QueryPatternService(
      QueryPatternRepository queryPatternRepository, FieldRepository fieldRepository) {
    this.queryPatternRepository = queryPatternRepository;
    this.fieldRepository = fieldRepository;
  }

  public List<QueryPattern> getAllQueryPatterns(String projectId) {
    return queryPatternRepository.findAllByProjectId(projectId);
  }

  /*
  public List<Field> getAllFields(String projectId) {
    return fieldRepository.findAllByProjectId(projectId);
  }

  @Transactional
  public void deleteAllFields(String projectId) {
    fieldRepository.deleteAllByProjectId(projectId);
  }*/

  @Transactional
  public void deleteAllQueryPatterns(String projectId) {
    queryPatternRepository.deleteAllByProjectId(projectId);
  }
}
