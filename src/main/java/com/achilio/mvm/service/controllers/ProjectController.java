package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextStripeCustomerId;
import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.services.StripeService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ProjectController {

  private final ProjectService projectService;
  private final StripeService stripeService;

  public ProjectController(ProjectService projectService, StripeService stripeService) {
    this.projectService = projectService;
    this.stripeService = stripeService;
  }

  @GetMapping(path = "/project", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all projects")
  public List<ProjectResponse> getAllProjects() {
    return projectService.getAllActivatedProjects(getContextTeamName()).stream()
        .map(this::toProjectResponse)
        .collect(Collectors.toList());
  }

  @GetMapping(path = "/project/{projectId}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get a project for a given projectId")
  public ProjectResponse getProject(@PathVariable final String projectId) {
    return toProjectResponse(projectService.getProject(projectId, getContextTeamName()));
  }

  @PostMapping(path = "/project", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Register a project if not exists")
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse createProject(@RequestBody final ACreateProjectRequest payload) {
    Project project = projectService.createProject(payload, getContextTeamName());
    refreshStripeProjectQuantity();
    return toProjectResponse(project);
  }

  private void refreshStripeProjectQuantity() {
    Long quantity = (long) projectService.getAllActivatedProjects(getContextTeamName()).size();
    stripeService.updateSubscriptionQuantity(getContextStripeCustomerId(), quantity);
  }

  @DeleteMapping(path = "/project/{projectId}")
  @ApiOperation("Unregister and delete a project")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProject(@PathVariable final String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    projectService.deleteProject(projectId);
    refreshStripeProjectQuantity();
  }

  @PatchMapping(path = "/project/{projectId}")
  @ApiOperation("Update metadata of a project")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ProjectResponse updateProject(
      @PathVariable final String projectId, @RequestBody final UpdateProjectRequest payload) {
    projectService.getProject(projectId, getContextTeamName());
    Project updatedProject = projectService.updateProject(projectId, payload);
    return toProjectResponse(updatedProject);
  }

  private ProjectResponse toProjectResponse(Project project) {
    return new ProjectResponse(project);
  }

}
