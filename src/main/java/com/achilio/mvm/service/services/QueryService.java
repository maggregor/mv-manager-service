package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

  private final QueryRepository queryRepository;

  public QueryService(QueryRepository queryRepository) {
    this.queryRepository = queryRepository;
  }

  public List<Query> getAllQueries(String projectId) {
    return queryRepository.findAllByProjectId(projectId);
  }

  public List<Query> getAllQueriesSince(String projectId, int timeframe) {
    Date in = new Date();
    LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
    Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    return getAllQueriesSince(projectId, out);
  }

  public List<Query> getAllQueriesSince(String projectId, Date date) {
    return queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, date);
  }

  public Query getQuery(String projectId, String queryId) {
    Optional<Query> query = queryRepository.findQueryByIdAndProjectId(queryId, projectId);
    if (!query.isPresent()) {
      throw new QueryNotFoundException(queryId);
    }
    return query.get();
  }
}
