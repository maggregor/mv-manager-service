package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.exceptions.FindMVJobNotFoundException;
import com.achilio.mvm.service.repositories.FindMVJobRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FindMVJobService {

  FindMVJobRepository repository;

  public FindMVJobService(FindMVJobRepository repository) {
    this.repository = repository;
  }

  private Optional<FindMVJob> findMVJob(Long id, String projectId) {
    return repository.findByIdAndProjectId(id, projectId);
  }

  private Optional<FindMVJob> findLastMVJob(String projectId) {
    return repository.findTopByProjectIdOrderByCreatedAtDesc(projectId);
  }

  private Optional<FindMVJob> findLastMVJob(String projectId, JobStatus status) {
    if (status == null) {
      return findLastMVJob(projectId);
    }
    return repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
  }

  public List<FindMVJob> getAllMVJobs(String projectId, JobStatus status) {
    if (status != null) {
      return repository.findAllByProjectIdAndStatus(projectId, status);
    }
    return repository.findAllByProjectId(projectId);
  }

  public FindMVJob getLastMVJobByStatus(String projectId, JobStatus status) {
    return findLastMVJob(projectId, status)
        .orElseThrow(() -> new FindMVJobNotFoundException("last"));
  }

  public FindMVJob getMVJob(Long id, String projectId) {
    return findMVJob(id, projectId)
        .orElseThrow(() -> new FindMVJobNotFoundException(id.toString()));
  }

  public FindMVJob createMVJob(String projectId, int timeframe) {
    FindMVJob job = new FindMVJob(projectId, timeframe);
    startMVJob(job);
    return job;
  }

  private void startMVJob(FindMVJob job) {
    // TODO: Start the optimization with the new MaterializedViewService class
  }
}
