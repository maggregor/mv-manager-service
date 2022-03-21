package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
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

  public List<Query> getAllQueriesByJobIdAndProjectId(Long fetcherJobId, String projectId) {
    Optional<FetcherQueryJob> job =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(fetcherJobId, projectId);
    if (!job.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherJobId.toString());
    }
    return queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job.get(), projectId);
  }

  public List<Query> getAllQueriesByProjectIdLastJob(String projectId) {
    Optional<FetcherQueryJob> job =
        fetcherJobRepository.findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(projectId);
    if (!job.isPresent()) {
      throw new ProjectNotFoundException(projectId);
    }
    return queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job.get(), projectId);
  }

  public Query getQuery(String queryId, String projectId) {
    Optional<Query> query = queryRepository.findQueryByIdAndProjectId(queryId, projectId);
    if (!query.isPresent()) {
      throw new QueryNotFoundException(queryId);
    }
    return query.get();
  }
}
