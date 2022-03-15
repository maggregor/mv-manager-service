package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<Query, String> {
  List<Query> findAllByInitialFetcherQueryJobAndLastFetcherQueryJob_ProjectId(
      FetcherQueryJob initialFetcherQueryJob, String projectId);

  List<Query> findAllByLastFetcherQueryJobAndLastFetcherQueryJob_ProjectId(
      FetcherQueryJob lastFetcherQueryJob, String projectId);

  Optional<Query> findQueryByIdAndLastFetcherQueryJob_ProjectId(String id, String projectId);
}
