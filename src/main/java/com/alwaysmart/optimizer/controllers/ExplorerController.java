package com.alwaysmart.optimizer.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import com.alwaysmart.optimizer.databases.entities.FetchedProject;

import com.alwaysmart.optimizer.services.FetcherService;
import com.alwaysmart.optimizer.services.MetadataService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ExplorerController {

    private static Logger LOGGER = LoggerFactory.getLogger(ExplorerController.class);

    @Autowired
    private MetadataService metadataService;
    @Autowired
    private FetcherService fetcherService;

    @GetMapping(path = "/project", produces = "application/json")
    @ApiOperation("List the project")
    public List<ProjectResponse> getAllProjects() {
        return fetcherService.fetchAllProjects()
                .stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/project/{projectId}", produces = "application/json")
    @ApiOperation("Get a project for a given projectId")
    public ProjectResponse getProject(@RequestHeader("Authorization") String accessTokenString, @PathVariable final String projectId) {
        return toProjectResponse(fetcherService.fetchProject(projectId));
    }

    @PutMapping(path = "/project/{projectId}", produces = "application/json")
    @ApiOperation("Update metadata of a project")
    public void updateProject(@PathVariable final String projectId, @RequestBody final UpdateProjectRequest request) {
        metadataService.updateProject(projectId, request.isActivated());
    }

    public ProjectResponse toProjectResponse(FetchedProject project) {
        final String projectId = project.getProjectId();
        boolean activated = metadataService.isProjectActivated(projectId);
        return new ProjectResponse(projectId, "Free Plan", activated, project.getDatasets());
    }

}
