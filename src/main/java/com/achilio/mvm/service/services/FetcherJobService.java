package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class FetcherJobService {

  private final FetcherService fetcherService;
  private final FetcherJobRepository fetcherJobRepository;

  public FetcherJobService(
      FetcherService fetcherService,
      ProjectService projectService,
      FetcherJobRepository fetcherJobRepository,
      ADatasetRepository datasetRepository,
      ATableRepository tableRepository) {
    this.fetcherService = fetcherService;
    this.fetcherJobRepository = fetcherJobRepository;
  }

  // Struct

  public Optional<FetcherStructJob> getLastFetcherStructJob(String projectId, JobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(projectId);
    }
    return fetcherJobRepository.findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
        projectId, status);
  }

  public List<FetcherStructJob> getAllStructJobs(String projectId, JobStatus status) {
    if (status == null) {
      return fetcherJobRepository.findFetcherStructJobsByProjectId(projectId);
    }
    return fetcherJobRepository.findFetcherStructJobsByProjectIdAndStatus(projectId, status);
  }

  public Optional<FetcherStructJob> getFetcherStructJob(Long fetcherQueryJobId, String projectId) {
    return fetcherJobRepository.findFetcherStructJobByIdAndProjectId(fetcherQueryJobId, projectId);
  }

  public FetcherStructJob createNewFetcherStructJob(String projectId) {
    FetcherStructJob job;
    job = new FetcherStructJob(projectId);
    return fetcherJobRepository.save(job);
  }

}
