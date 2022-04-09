package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.FindMVJobRequest;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FindMVJobService {

  public Optional<FindMVJob> findLastMVJob(String projectId) {
    return findLastMVJob(projectId, null);
  }

  public Optional<FindMVJob> findLastMVJob(String projectId, JobStatus status) {
    return Optional.empty();
  }

  public FindMVJob getLastMVJob(String projectId) {
    return null;
  }

  public List<FindMVJob> getAllMVJobs(String projectId, JobStatus status) {
    return Collections.emptyList();
  }

  public FindMVJob getMVJob(String projectId, Long id) {
    return null;
  }

  public FindMVJob createMVJob(FindMVJobRequest payload) {
    FindMVJob job = new FindMVJob(payload.getProjectId(), payload.getTimeframe());
    startMVJob(job);
    return job;
  }

  @Async
  void startMVJob(FindMVJob job) {
    return;
  }
}
