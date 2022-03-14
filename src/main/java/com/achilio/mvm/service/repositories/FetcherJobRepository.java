package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<FetcherQueryJob, Long> {
  /** Find all jobs */
  List<FetcherQueryJob> findFetcherQueryJobsByProjectId(String projectId);

  List<FetcherQueryJob> findFetcherQueryJobsByProjectIdAndStatus(
      String projectId, FetcherJobStatus status);

  /** Find single job */
  Optional<FetcherQueryJob> findFetcherQueryJobByProjectIdAndId(String projectId, Long id);

  /** Find last job */
  Optional<FetcherQueryJob> findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherQueryJob> findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);
}
