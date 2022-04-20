package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.controllers.requests.FetcherStructJobRequest;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  @Qualifier("fetchQueryJob")
  private Job fetcherQueryJob;

  @Autowired
  @Qualifier("fetchStructJob")
  private Job fetcherStructJob;

  public BatchJobController(JobLauncher jobLauncher, ProjectService projectService) {
    this.jobLauncher = jobLauncher;
    this.projectService = projectService;
  }

  @SneakyThrows
  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public String createNewFetcherQueryJob(@RequestBody FetcherQueryJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("projectId", projectId)
            .toJobParameters();
    jobLauncher.run(fetcherQueryJob, jobParameters);
    return "Oui oui c'est bon";
  }

  @SneakyThrows
  @PostMapping(path = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public String fetchStruct(@RequestBody FetcherStructJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId, getContextTeamName());
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("projectId", projectId)
            .toJobParameters();
    jobLauncher.run(fetcherStructJob, jobParameters);
    return "Ah bah là... c'est bon, je te jure !";
  }
}
