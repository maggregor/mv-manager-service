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

  // Query
  /** Find single job */
  Optional<FetcherQueryJob> findFetcherQueryJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherQueryJob> findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherQueryJob> findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);

  // Struct
  /** Find single job */
  Optional<FetcherStructJob> findFetcherStructJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherStructJob> findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherStructJob> findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);

  // Any
  /** Find single job */
  Optional<FetcherJob> findFetcherJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherJob> findTopFetcherJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherJob> findTopFetcherJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, FetcherJobStatus status);
}
