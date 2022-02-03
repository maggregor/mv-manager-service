package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizerRepository extends JpaRepository<Optimization, Integer> {

  Optional<Optimization> findAllByProjectId(String projectId);

  List<OptimizationResult> findAllByDatasetName(String datasetName);
}
