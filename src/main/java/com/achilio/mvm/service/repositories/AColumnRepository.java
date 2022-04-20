package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.AColumn;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AColumnRepository extends CrudRepository<AColumn, Long> {

  List<AColumn> findAllByTable_ProjectId(String projectId);
}
