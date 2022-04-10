package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Job;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<Job, Long> {
  /** Find all jobs */
  List<Job> findFetcherJobsByProjectId(String projectId);

  List<Job> findFetcherJobsByProjectIdAndStatus(String projectId, JobStatus status);

  List<FetcherQueryJob> findFetcherQueryJobsByProjectId(String projectId);

  List<FetcherQueryJob> findFetcherQueryJobsByProjectIdAndStatus(
      String projectId, JobStatus status);

  List<FetcherStructJob> findFetcherStructJobsByProjectId(String projectId);

  List<FetcherStructJob> findFetcherStructJobsByProjectIdAndStatus(
      String projectId, JobStatus status);

  // Query
  /** Find single job */
  Optional<FetcherQueryJob> findFetcherQueryJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherQueryJob> findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(String projectId);

  Optional<FetcherQueryJob> findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, JobStatus status);

  // Struct
  /** Find single job */
  Optional<FetcherStructJob> findFetcherStructJobByIdAndProjectId(Long id, String projectId);

  /** Find last job */
  Optional<FetcherStructJob> findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(
      String projectId);

  Optional<FetcherStructJob> findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, JobStatus status);
}
