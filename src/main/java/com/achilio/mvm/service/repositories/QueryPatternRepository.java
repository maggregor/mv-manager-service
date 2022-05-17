package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.QueryPattern;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryPatternRepository extends JpaRepository<QueryPattern, Long> {

  List<QueryPattern> findAllByProjectId(String projectId);

  void deleteAllByProjectId(String projectId);
}
