package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

  Optional<Project> findByProjectId(String projectId);
}
