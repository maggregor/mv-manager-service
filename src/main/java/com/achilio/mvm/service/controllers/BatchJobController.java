package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.FetcherDataModelJobRequest;
import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
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

  private final JobLauncher jobLauncher;
  private final ProjectService projectService;

  private final Job fetcherQueryJob;

  private final Job fetchDataModel;

  public BatchJobController(JobLauncher jobLauncher, ProjectService projectService,
      @Qualifier("fetchDataModelJob") Job fetchDataModel,
      @Qualifier("fetchQueryJob") Job fetcherQueryJob) {
    this.jobLauncher = jobLauncher;
    this.projectService = projectService;
    this.fetchDataModel = fetchDataModel;
    this.fetcherQueryJob = fetcherQueryJob;
  }

  @SneakyThrows
  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public void createNewFetcherQueryJob(@RequestBody FetcherQueryJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("projectId", projectId)
            .addString("teamName", getContextTeamName())
            .addLong("timeframe", payload.getTimeframe().longValue())
            .toJobParameters();
    jobLauncher.run(fetcherQueryJob, jobParameters);
  }

  @SneakyThrows
  @PostMapping(path = "/data-model", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new data model fetching job")
  public void fetchDataModel(@RequestBody FetcherDataModelJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("projectId", projectId)
            .addString("teamName", getContextTeamName())
            .toJobParameters();
    jobLauncher.run(fetchDataModel, jobParameters);
  }
}
