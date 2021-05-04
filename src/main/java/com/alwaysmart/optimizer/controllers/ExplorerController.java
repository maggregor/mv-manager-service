package com.alwaysmart.optimizer.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import com.alwaysmart.optimizer.databases.entities.FetchedProject;

import com.alwaysmart.optimizer.services.FetcherService;
import com.alwaysmart.optimizer.services.MetadataService;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
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

    @GetMapping(path = "/project", produces = "application/json")
    public List<ProjectResponse> getAllProjects(@RequestHeader("Authorization") String accessTokenString) {
        return new FetcherService(buildGoogleCredentials(accessTokenString)).fetchAllProjects()
                .stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/project/{projectId}", produces = "application/json")
    public ProjectResponse getProject(@RequestHeader("Authorization") String accessTokenString, @PathVariable final String projectId) {
        return toProjectResponse(new FetcherService(buildGoogleCredentials(accessTokenString)).fetchProject(projectId));
    }

    @PutMapping(path = "/project/{projectId}", produces = "application/json")
    public void updateProject(@PathVariable final String projectId, @RequestBody final UpdateProjectRequest request) {
        metadataService.updateProject(projectId, request.isActivated());
    }

    public ProjectResponse toProjectResponse(FetchedProject project) {
        final String projectId = project.getProjectId();
        boolean activated = metadataService.isProjectActivated(projectId);
        return new ProjectResponse(projectId, "Free Plan", activated, project.getDatasets());
    }


    private GoogleCredentials buildGoogleCredentials(String accessTokenString) {
        AccessToken accessToken = new AccessToken(accessTokenString, new GregorianCalendar(2021, Calendar.JUNE, 25, 5, 0).getTime());
        return UserCredentials.newBuilder()
                .setClientId("293325499254-8h7bv5piflnjdoufjak8jjh03tpqss8b.apps.googleusercontent.com")
                .setClientSecret("OZeUkdunGSTQ7ZyvM7oDtabD")
                .setAccessToken(accessToken)
                .build();
    }


}
