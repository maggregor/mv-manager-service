package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<Query, String> {
  List<Query> findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
      FetcherQueryJob fetcherQueryJob, String projectId);

  Optional<Query> findFirstByIdAndFetcherQueryJob_ProjectId(String id, String projectId);

  Optional<Query> findQueryByIdAndProjectId(String id, String projectId);
}
