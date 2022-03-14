package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<Query, Long> {
  List<Query> findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
      FetcherQueryJob fetcherQueryJob, String projectId);
}
