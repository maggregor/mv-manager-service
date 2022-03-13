package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.AchilioQuery;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.repositories.AchilioQueryRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class FetcherJobService {

  @Autowired private FetcherService fetcherService;
  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private AchilioQueryRepository achilioQueryRepository;

  public FetcherJobService() {}

  @Async("asyncExecutor")
  public void fetchQueriesJobSince(FetcherQueryJob fetcherQueryJob) {
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.WORKING);
    try {
      fetchAndSaveQueries(fetcherQueryJob);
    } catch (Exception e) {
      updateJobStatus(fetcherQueryJob, FetcherJobStatus.ERROR);
      throw e;
    }
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.FINISHED);
  }

  private void fetchAndSaveQueries(FetcherQueryJob fetcherQueryJob) {
    List<FetchedQuery> allQueries =
        fetcherService.fetchQueriesSince(
            fetcherQueryJob.getProjectId(), fetcherQueryJob.getTimeframe());
    List<AchilioQuery> achilioQueries =
        allQueries.stream()
            .map(q -> toAchilioQuery(q, fetcherQueryJob))
            .map(this::saveQuery)
            .collect(Collectors.toList());
  }

  private AchilioQuery toAchilioQuery(FetchedQuery fetchedQuery, FetcherQueryJob job) {
    Set<String> refTables =
        fetchedQuery.getReferenceTables().stream()
            .map(FetchedTable::getTableName)
            .collect(Collectors.toSet());
    return new AchilioQuery(
        job,
        fetchedQuery.getQuery(),
        fetchedQuery.isUsingMaterializedView(),
        fetchedQuery.isUsingCache(),
        fetchedQuery.getDate(),
        refTables,
        fetchedQuery.getStatistics());
  }

  private AchilioQuery saveQuery(AchilioQuery achilioQuery) {
    return achilioQueryRepository.save(achilioQuery);
  }

  private void updateJobStatus(FetcherQueryJob job, FetcherJobStatus status) {
    job.setStatus(status);
    fetcherJobRepository.save(job);
  }
}
