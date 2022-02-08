package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Optimization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizerRepository extends JpaRepository<Optimization, Integer> {

  List<Optimization> findAllByProjectId(String projectId);

}
