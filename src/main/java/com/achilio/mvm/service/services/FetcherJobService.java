package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.util.List;
import java.util.Optional;
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

  public Optional<FetcherQueryJob> getLastFetcherQueryJob(String projectId) {
    return fetcherJobRepository.findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(projectId);
  }

  public Optional<FetcherQueryJob> getLastFetcherQueryJob(
      String projectId, FetcherJobStatus status) {
    return fetcherJobRepository.findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public List<FetcherQueryJob> getAllQueryJobs(String projectId, FetcherJobStatus status) {
    return fetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(projectId, status);
  }

  public List<FetcherQueryJob> getAllQueryJobs(String projectId) {
    return fetcherJobRepository.findFetcherQueryJobsByProjectId(projectId);
  }

  public Optional<FetcherQueryJob> getFetcherQueryJob(Long fetcherQueryJobId, String projectId) {
    return fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(fetcherQueryJobId, projectId);
  }

  public FetcherQueryJob createNewFetcherQueryJob(
      String projectId, FetcherQueryJobRequest payload) {
    FetcherQueryJob job;
    if (payload.getTimeframe() != null) {
      job = new FetcherQueryJob(projectId, payload.getTimeframe());
    } else {
      job = new FetcherQueryJob(projectId);
    }
    return fetcherJobRepository.save(job);
  }

  @Async("asyncExecutor")
  public void fetchAllQueriesJob(FetcherQueryJob fetcherQueryJob) {
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

  @Transactional
  void updateJobStatus(FetcherJob job, FetcherJobStatus status) {
    job.setStatus(status);
    fetcherJobRepository.save(job);
  }

  // Struct

  public Optional<FetcherStructJob> getLastFetcherStructJob(
      String projectId, FetcherJobStatus status) {
    return fetcherJobRepository.findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public Optional<FetcherStructJob> getLastFetcherStructJob(String projectId) {
    return fetcherJobRepository.findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(projectId);
  }

  public List<FetcherStructJob> getAllStructJobs(String projectId, FetcherJobStatus status) {
    return fetcherJobRepository.findFetcherStructJobsByProjectIdAndStatus(projectId, status);
  }

  public List<FetcherStructJob> getAllStructJobs(String projectId) {
    return fetcherJobRepository.findFetcherStructJobsByProjectId(projectId);
  }

  public Optional<FetcherStructJob> getFetcherStructJob(Long fetcherQueryJobId, String projectId) {
    return fetcherJobRepository.findFetcherStructJobByIdAndProjectId(fetcherQueryJobId, projectId);
  }

  public FetcherStructJob createNewFetcherStructJob(String projectId) {
    FetcherStructJob job;
    job = new FetcherStructJob(projectId);
    return fetcherJobRepository.save(job);
  }

  //  @Async("asyncExecutor")
  //  public void fetchAllStructsJob(FetcherStructJob fetcherStructJob) {
  //    updateJobStatus(fetcherStructJob, FetcherJobStatus.WORKING);
  //    try {
  //      List<Query> queries = fetchQueries(fetcherStructJob);
  //      saveAllQueries(queries);
  //    } catch (Exception e) {
  //      updateJobStatus(fetcherStructJob, FetcherJobStatus.ERROR);
  //      throw e;
  //    }
  //    updateJobStatus(fetcherStructJob, FetcherJobStatus.FINISHED);
  //  }
}
