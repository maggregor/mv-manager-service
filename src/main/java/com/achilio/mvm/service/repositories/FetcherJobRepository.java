package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<FetcherQueryJob, Long> {
  List<FetcherJob> findFetcherQueryJobsByProjectId(String projectId);

  Optional<FetcherJob> findFetcherQueryJobByProjectIdAndId(String projectId, Long id);

  /** Find last job */
  Optional<FetcherJob> findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);
}
