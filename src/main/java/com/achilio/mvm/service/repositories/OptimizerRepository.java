package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Optimization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptimizerRepository extends JpaRepository<Optimization, Integer> {

	Optional<Optimization> findAllByProjectId(String projectId);

}