package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.services.FindMVJobService;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/job/")
@Validated
public class FindMVJobController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetcherJobController.class);

  private final ProjectService projectService;
  private final FindMVJobService mvJobService;

  public FindMVJobController(FindMVJobService mvJobService, ProjectService projectService) {
    this.mvJobService = mvJobService;
    this.projectService = projectService;
  }

  @GetMapping(path = "/mv", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public List<FindMVJob> getAllMaterializedViews(
      @RequestParam String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) JobStatus status) {
    projectService.getProject(projectId, getContextTeamName());
    if (Boolean.TRUE == last) {
      Optional<FindMVJob> optionalMVJob = mvJobService.getLastMVJob(projectId, status);
      return optionalMVJob.map(Collections::singletonList).orElse(Collections.emptyList());
    }
    return mvJobService.getAllMVJobs(projectId, status);
  }
}
