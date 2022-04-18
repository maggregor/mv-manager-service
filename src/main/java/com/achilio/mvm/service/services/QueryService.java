package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics.Scope;
import com.achilio.mvm.service.entities.statistics.QueryStatistics;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public GlobalQueryStatistics getStatistics(String projectId, LocalDate from) {
    return getStatistics(getAllQueriesSince(projectId, from));
  }

  public GlobalQueryStatistics getStatistics(List<Query> queries) {
    // Select using materialized view
    List<Query> selectIn =
        queries.stream().filter(Query::isUseMaterializedView).collect(Collectors.toList());
    // Select using cache
    List<Query> selectCached =
        queries.stream().filter(Query::isUseCache).collect(Collectors.toList());
    // Select using table source
    List<Query> selectOut =
        queries.stream()
            .filter(q -> !q.isUseMaterializedView() && !q.isUseCache())
            .collect(Collectors.toList());
    GlobalQueryStatistics global = new GlobalQueryStatistics();
    global.addStatistic(Scope.IN, new QueryStatistics(selectIn));
    global.addStatistic(Scope.OUT, new QueryStatistics(selectOut));
    global.addStatistic(Scope.CACHED, new QueryStatistics(selectCached));
    return global;
  }
}
