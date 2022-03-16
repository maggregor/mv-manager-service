package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<FetcherJob, Long> {
  /** Find all jobs */
  List<FetcherJob> findFetcherJobsByProjectId(String projectId);

  List<FetcherJob> findFetcherJobsByProjectIdAndStatus(String projectId, FetcherJobStatus status);

  List<FetcherQueryJob> findFetcherQueryJobsByProjectId(String projectId);

  List<FetcherQueryJob> findFetcherQueryJobsByProjectIdAndStatus(
      String projectId, FetcherJobStatus status);

  List<FetcherStructJob> findFetcherStructJobsByProjectId(String projectId);

  List<FetcherStructJob> findFetcherStructJobsByProjectIdAndStatus(
      String projectId, FetcherJobStatus status);

  /** Find single job */
  Optional<FetcherQueryJob> findFetcherQueryJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherQueryJob> findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherQueryJob> findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);
}
