package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.AColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AColumnRepository extends JpaRepository<AColumn, Long> {
  List<AColumn> findAllByTable_Project_ProjectId(String projectId);
}
