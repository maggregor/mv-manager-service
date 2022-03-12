package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.FetcherJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetcherJobRepository extends JpaRepository<FetcherJob, Long> {
  List<FetcherJob> findAllByProjectId(String projectId);

  /** Find last job */
  FetcherJob findTopByProjectIdOrderByCreatedAtDesc(String projectId);
}
