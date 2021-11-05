package com.achilio.mvm.service.services;

import com.achilio.mvm.service.entities.ProjectMetadata;
import com.achilio.mvm.service.repositories.ProjectMetadataRepository;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class MetadataService {

  @Autowired
  private ProjectMetadataRepository projectRepository;

  public MetadataService() {
  }

  public boolean isProjectActivated(String projectId) {
    Optional<ProjectMetadata> projectMetadata = getProject(projectId);
    return projectMetadata.isPresent() && projectMetadata.get().isActivated();
  }

  public Optional<ProjectMetadata> getProject(String projectId) {
    return projectRepository.findByProjectId(projectId);
  }

  public boolean projectExists(String projectId) {
    return getProject(projectId).isPresent();
  }

  public void registerProjectIfNotExists(String projectId, Boolean activated) {
    if (!projectExists(projectId)) {
      registerProject(projectId, activated);
    }
  }

  public void registerProject(String projectId, Boolean activated) {
    ProjectMetadata projectMetadata = new ProjectMetadata(projectId, activated);
    projectRepository.save(projectMetadata);
  }

  @Transactional
  public void updateProject(String projectId, Boolean activated) {
    registerProjectIfNotExists(projectId, activated);
    ProjectMetadata projectMetadata = getProject(projectId).get();
    projectMetadata.setActivated(activated);
    projectRepository.save(projectMetadata);
  }
}
