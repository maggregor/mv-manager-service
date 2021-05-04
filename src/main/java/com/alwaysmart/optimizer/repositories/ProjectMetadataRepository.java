package com.alwaysmart.optimizer.repositories;

import com.alwaysmart.optimizer.entities.ProjectMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMetadataRepository extends JpaRepository<ProjectMetadata, Integer> {

	Optional<ProjectMetadata> findByProjectId(String projectId);

}