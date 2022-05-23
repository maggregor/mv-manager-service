package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Field;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field, String> {

/*  void deleteAllByProjectId(String projectId);

  List<Field> findAllByProjectId(String projectId);*/
}
