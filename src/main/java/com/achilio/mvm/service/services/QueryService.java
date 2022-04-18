package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class QueryService {

  private final QueryRepository queryRepository;

  public QueryService(QueryRepository queryRepository) {
    this.queryRepository = queryRepository;
  }

  public List<Query> getAllQueries(String projectId) {
    return queryRepository.findAllByProjectId(projectId);
  }

  public List<Query> getAllQueriesSince(String projectId, LocalDate date) {
    return queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, date);
  }

  public Query getQuery(String queryId, String projectId) {
    Optional<Query> query = queryRepository.findQueryByIdAndProjectId(queryId, projectId);
    if (!query.isPresent()) {
      throw new QueryNotFoundException(queryId);
    }
    return query.get();
  }
}
