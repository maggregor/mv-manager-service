package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
public class QueryService {

  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private QueryRepository queryRepository;

  public List<Query> getAllQueriesByJobIdAndProjectId(String fetcherJobId, String projectId) {
    Optional<FetcherQueryJob> job =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(
            Long.valueOf(fetcherJobId), projectId);
    if (!job.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherJobId);
    }
    return queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
        job.get(), projectId);
  }

  public List<Query> getAllQueriesByProjectIdLastJob(String projectId) {
    Optional<FetcherQueryJob> job =
        fetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(projectId);
    if (!job.isPresent()) {
      throw new FetcherJobNotFoundException("last");
    }
    return queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
        job.get(), projectId);
  }
}
