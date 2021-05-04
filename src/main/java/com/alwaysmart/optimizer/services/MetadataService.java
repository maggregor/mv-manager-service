package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.entities.ProjectMetadata;
import com.alwaysmart.optimizer.repositories.ProjectMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class MetadataService {

    @Autowired
    private ProjectMetadataRepository projectRepository;

    public MetadataService() {}


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
        if(!projectExists(projectId)) {
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