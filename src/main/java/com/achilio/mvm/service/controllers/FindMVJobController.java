package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.FindMVJobRequest;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.services.FindMVJobService;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @ApiOperation("List all find mv job for a given projectId. Can filter by status")
  public List<FindMVJob> getAllFindMVJobs(
      @RequestParam String projectId, @RequestParam(required = false) JobStatus status) {
    projectService.getProject(projectId, getContextTeamName());
    return mvJobService.getAllMVJobs(projectId, status);
  }

  @GetMapping(path = "/mv/{mvId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "Get a single find mv job for a given projectId.\n"
          + "If mvId is equals to last, returns the last job based on creation date")
  public FindMVJob getFindMVJob(@PathVariable String mvId, @RequestParam String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    if (mvId.toLowerCase(Locale.ROOT).equals("last")) {
      return mvJobService.getLastMVJob(projectId);
    }
    Long id = Long.valueOf(mvId);
    return mvJobService.getMVJob(projectId, id);
  }

  @PostMapping(path = "/mv", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "Launch a findMVJob. The job is async but the controller returns the object"
          + "with the status PENDING.")
  public FindMVJob startFindMVJob(@Valid @RequestBody FindMVJobRequest payload) {
    projectService.getProject(payload.getProjectId(), getContextTeamName());
    return mvJobService.createMVJob(payload);
  }
}
