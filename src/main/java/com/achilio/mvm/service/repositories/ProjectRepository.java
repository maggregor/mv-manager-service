package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

  Optional<Project> findByProjectId(String projectId);

  List<Project> findAllByActivatedAndTeamName(Boolean activated, String teamName);

  Optional<Project> findByProjectIdAndTeamName(String projectId, String teamName);
}
