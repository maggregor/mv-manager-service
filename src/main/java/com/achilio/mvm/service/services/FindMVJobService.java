package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FindMVJobService {

  public Optional<FindMVJob> getLastMVJob(String projectId, JobStatus status) {
    return Optional.empty();
  }

  public List<FindMVJob> getAllMVJobs(String projectId, JobStatus status) {
    return Collections.emptyList();
  }
}
