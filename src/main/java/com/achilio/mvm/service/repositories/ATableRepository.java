package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ATable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATableRepository extends CrudRepository<ATable, String> {

  List<ATable> findAllByProjectId(String projectId);
  
  Optional<ATable> findByTableId(String tableId);

  void deleteByTableId(String tableId);
}
