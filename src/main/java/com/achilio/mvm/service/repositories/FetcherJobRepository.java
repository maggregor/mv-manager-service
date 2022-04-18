package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Job;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FetcherJobRepository extends JpaRepository<Job, Long> {

  List<FetcherStructJob> findFetcherStructJobsByProjectId(String projectId);

  List<FetcherStructJob> findFetcherStructJobsByProjectIdAndStatus(
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
