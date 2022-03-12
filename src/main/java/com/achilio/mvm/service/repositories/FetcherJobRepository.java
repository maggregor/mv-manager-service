package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<FetcherQueryJob, Long> {
  /** Find all jobs */
  List<FetcherJob> findFetcherQueryJobsByProjectId(String projectId);

  List<FetcherJob> findFetcherQueryJobsByProjectIdAndStatus(
      String projectId, FetcherJobStatus status);

  /** Find single job */
  Optional<FetcherJob> findFetcherQueryJobByProjectIdAndId(String projectId, Long id);

  /** Find last job */
  Optional<FetcherJob> findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherJob> findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);
}
