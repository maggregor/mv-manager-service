package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.ExtractQueryPatternJobRequest;
import com.achilio.mvm.service.controllers.requests.FetcherDataModelJobRequest;
import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.services.BatchJobService;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/job/batch")
@Validated
public class BatchJobController {

  private final ProjectService projectService;
  private final BatchJobService service;

  public BatchJobController(BatchJobService service, ProjectService projectService) {
    this.service = service;
    this.projectService = projectService;
  }

  @SneakyThrows
  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public void startNewFetcherQueryJob(@RequestBody FetcherQueryJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    service.runFetcherQueryJob(getContextTeamName(), projectId, payload.getTimeframe());
  }

  @SneakyThrows
  @PostMapping(path = "/data-model", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new data model fetching job")
  public void startNewDataModelQueryJob(@RequestBody FetcherDataModelJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    service.runFetcherDataModelJob(getContextTeamName(), projectId);
  }

  @SneakyThrows
  @PostMapping(path = "/query-pattern", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new data model fetching job")
  public void startNewQueryPatternExtract(@RequestBody ExtractQueryPatternJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    service.runFetcherExtractJob(getContextTeamName(), projectId);
  }
}
