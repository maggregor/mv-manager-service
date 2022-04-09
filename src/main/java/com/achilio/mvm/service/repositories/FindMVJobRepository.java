package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FindMVJobRepository extends JpaRepository<FindMVJob, Long> {

  Optional<FindMVJob> findTopByProjectIdAndStatusOrderByCreatedAtDesc(
      String projectId, JobStatus status);

  Optional<FindMVJob> findTopByProjectIdOrderByCreatedAtDesc(String projectId);

  List<FindMVJob> findAllByProjectId(String projectId);

  List<FindMVJob> findAllByProjectIdAndStatus(String projectId, JobStatus status);

  Optional<FindMVJob> findByIdAndProjectId(Long id, String projectId);
}
