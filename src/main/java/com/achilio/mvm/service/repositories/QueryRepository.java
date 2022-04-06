package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<Query, String> {
  List<Query> findAllByInitialFetcherQueryJobAndProjectId(
      FetcherQueryJob initialFetcherQueryJob, String projectId);

  List<Query> findAllByLastFetcherQueryJobAndProjectId(
      FetcherQueryJob lastFetcherQueryJob, String projectId);

  Optional<Query> findQueryByIdAndProjectId(String id, String projectId);

  List<Query> findAllByProjectId(String projectId);

  List<Query> findAllByProjectIdAndStartTimeGreaterThanEqual(String projectId, LocalDate date);
}
