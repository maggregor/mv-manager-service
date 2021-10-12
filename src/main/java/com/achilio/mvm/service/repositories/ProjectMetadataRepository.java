package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.ProjectMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMetadataRepository extends JpaRepository<ProjectMetadata, Integer> {

  Optional<ProjectMetadata> findByProjectId(String projectId);
}
