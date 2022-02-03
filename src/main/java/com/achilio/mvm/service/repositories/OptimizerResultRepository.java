package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.OptimizationResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizerResultRepository extends JpaRepository<OptimizationResult, Integer> {
  List<OptimizationResult> findAllByOptimizationId(Long optimizationId);
}
