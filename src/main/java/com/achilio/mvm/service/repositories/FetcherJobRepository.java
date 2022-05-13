package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherDataModelJob;
import com.achilio.mvm.service.entities.Job;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FetcherJobRepository extends JpaRepository<Job, Long> {

  List<FetcherDataModelJob> findFetcherStructJobsByProjectId(String projectId);

  List<FetcherDataModelJob> findFetcherStructJobsByProjectIdAndStatus(
      String projectId, JobStatus status);

  // Struct

  /**
   * Find single job
   */
  Optional<FetcherDataModelJob> findFetcherStructJobByIdAndProjectId(Long id, String projectId);

  /**
   * Find last job
   */
  Optional<FetcherDataModelJob> findTopFetcherStructJobByProjectIdOrderByCreatedAtDesc(
      String projectId);

  Optional<FetcherDataModelJob> findTopFetcherStructJobByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, JobStatus status);
}
