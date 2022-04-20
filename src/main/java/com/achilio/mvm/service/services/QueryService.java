package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

  private final QueryRepository repository;

  public QueryService(QueryRepository queryRepository) {
    this.repository = queryRepository;
  }

  public List<AQuery> getAllQueries(String projectId) {
    return repository.findAllByProjectId(projectId);
  }

  public List<AQuery> getAllQueriesSince(String projectId, int timeframe) {
    return getAllQueriesSince(projectId, todayMinusDays(timeframe));
  }

  public List<AQuery> getAllQueriesSince(String projectId, Date date) {
    return repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, date);
  }

  public Long getAverageProcessedBytesSince(String projectId, int minusDays) {
    Date from = todayMinusDays(minusDays);
    return repository.averageProcessedBytesByProjectAndStartTimeGreaterThanEqual(projectId,
        from);
  }

  public Long getTotalQuerySince(String projectId, int minusDays) {
    Date from = todayMinusDays(minusDays);
    return repository.countQueryByProjectAndStartTimeGreaterThanEqual(projectId, from);
  }

  public Long getPercentQueryInMVSince(String projectId, int minusDays) {
    Date from = todayMinusDays(minusDays);
    Long total = repository.countQueryByProjectAndStartTimeGreaterThanEqual(projectId, from);
    Long inMV = repository.countQueryInMVByProjectAndStartTimeGreaterThanEqual(projectId, from);
    return (long) (inMV * 100.0 / total + 0.5);
  }

  public AQuery getQuery(String projectId, String queryId) {
    return repository.findQueryByProjectIdAndId(projectId, queryId)
        .orElseThrow(() -> new QueryNotFoundException(queryId));
  }

  private Date todayMinusDays(int minusDays) {
    LocalDate localDate = LocalDate.now().minusDays(minusDays);
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
