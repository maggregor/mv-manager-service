package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Optimization;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizerRepository extends JpaRepository<Optimization, Integer> {

  Optional<Optimization> findAllByProjectId(String projectId);

  Optional<Optimization> findAllByProjectIdDateAfter(String projectId, ZonedDateTime time);
}
