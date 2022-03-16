package com.achilio.mvm.service.services;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class FetcherJobService {

  //  private static final Logger LOGGER = LoggerFactory.getLogger(FetcherJobService.class);

  @Autowired private FetcherService fetcherService;
  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private QueryRepository queryRepository;

  public FetcherJobService() {}

  @Async("asyncExecutor")
  public void fetchQueriesJob(FetcherQueryJob fetcherQueryJob) {
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.WORKING);
    try {
      List<Query> queries = fetchQueries(fetcherQueryJob);
      saveAllQueries(queries);
    } catch (Exception e) {
      updateJobStatus(fetcherQueryJob, FetcherJobStatus.ERROR);
      throw e;
    }
    updateJobStatus(fetcherQueryJob, FetcherJobStatus.FINISHED);
  }

  public void fetchStructJob(FetcherStructJob fetcherStructJob) {
    updateJobStatus(fetcherStructJob, FetcherJobStatus.WORKING);
  }

  @Transactional
  void saveAllQueries(List<Query> queries) {
    queryRepository.saveAll(queries);
  }

  private List<Query> fetchQueries(FetcherQueryJob fetcherQueryJob) {
    List<FetchedQuery> allQueries =
        fetcherService.fetchQueriesSinceLastDays(
            fetcherQueryJob.getProjectId(), fetcherQueryJob.getTimeframe());
    return allQueries.stream()
        .map(q -> toAchilioQuery(q, fetcherQueryJob))
        .collect(Collectors.toList());
  }

  private Query toAchilioQuery(FetchedQuery fetchedQuery, FetcherQueryJob job) {
    return new Query(
        job,
        fetchedQuery.getQuery(),
        fetchedQuery.getGoogleJobId(),
        fetchedQuery.getProjectId(),
        fetchedQuery.isUsingMaterializedView(),
        fetchedQuery.isUsingCache(),
        fetchedQuery.getDate(),
        fetchedQuery.getStatistics());
  }

  private void updateJobStatus(FetcherJob job, FetcherJobStatus status) {
    job.setStatus(status);
    fetcherJobRepository.save(job);
  }
}
