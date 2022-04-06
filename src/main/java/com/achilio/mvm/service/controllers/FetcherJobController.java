package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.controllers.requests.FetcherStructJobRequest;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.services.FetcherJobService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/fetcher/job")
@Validated
public class FetcherJobController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetcherJobController.class);

  private final ProjectService projectService;
  private final FetcherJobService fetcherJobService;

  public FetcherJobController(FetcherJobService fetcherJobService, ProjectService projectService) {
    this.fetcherJobService = fetcherJobService;
    this.projectService = projectService;
  }

  // Fetcher for Queries

  @GetMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public List<FetcherQueryJob> getAllFetcherQueryJobsByProjectId(
      @RequestParam String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) FetcherJobStatus status) {
    projectService.getProject(projectId);
    if (Boolean.TRUE == last) {
      Optional<FetcherQueryJob> optionalFetcherJob;
      optionalFetcherJob = fetcherJobService.getLastFetcherQueryJob(projectId, status);
      return optionalFetcherJob.map(Collections::singletonList).orElse(Collections.EMPTY_LIST);
    }
    return fetcherJobService.getAllQueryJobs(projectId, status);
  }

  @GetMapping(path = "/query/{fetcherQueryJobId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a fetcher query job for a the given fetcherQueryJobId.")
  public FetcherQueryJob getFetcherQueryJob(
      @RequestParam String projectId, @PathVariable Long fetcherQueryJobId) {
    projectService.getProject(projectId);
    Optional<FetcherQueryJob> optionalFetcherJob =
        fetcherJobService.getFetcherQueryJob(fetcherQueryJobId, projectId);
    if (!optionalFetcherJob.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherQueryJobId.toString());
    }
    return optionalFetcherJob.get();
  }

  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public FetcherQueryJob createNewFetcherQueryJob(@RequestBody FetcherQueryJobRequest payload) {
    String projectId = payload.getProjectId();
    projectService.getProject(projectId);
    FetcherQueryJob currentJob = fetcherJobService.createNewFetcherQueryJob(projectId, payload);
    LOGGER.info("Starting FetcherQueryJob {}", currentJob.getId());
    fetcherJobService.fetchAllQueriesJob(currentJob, getContextTeamName());
    return currentJob;
  }

  // Fetcher for Structs

  @GetMapping(path = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query struct for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query struct")
  public List<FetcherStructJob> getAllFetcherStructJobsByProjectId(
      @RequestParam String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) FetcherJobStatus status) {
    projectService.getProject(projectId);
    if (Boolean.TRUE == last) {
      Optional<FetcherStructJob> optionalFetcherJob;
      optionalFetcherJob = fetcherJobService.getLastFetcherStructJob(projectId, status);
      return optionalFetcherJob.map(Collections::singletonList).orElse(Collections.EMPTY_LIST);
    }
    return fetcherJobService.getAllStructJobs(projectId, status);
  }

  @GetMapping(path = "/struct/{fetcherQueryJobId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a fetcher query job for a given id.")
  public FetcherStructJob getFetcherStructJob(
      @PathVariable Long fetcherQueryJobId, @RequestParam String projectId) {
    projectService.getProject(projectId);
    Optional<FetcherStructJob> optionalFetcherJob =
        fetcherJobService.getFetcherStructJob(fetcherQueryJobId, projectId);
    if (!optionalFetcherJob.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherQueryJobId.toString());
    }
    return optionalFetcherJob.get();
  }

  @PostMapping(path = "/struct", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new struct fetching job")
  public FetcherStructJob createNewFetcherStructJob(@RequestBody FetcherStructJobRequest req) {
    FetcherStructJob currentJob = fetcherJobService.createNewFetcherStructJob(req.getProjectId());
    LOGGER.info("Starting FetcherStructJob {}", currentJob.getId());
    fetcherJobService.syncAllStructsJob(currentJob, getContextTeamName());
    return currentJob;
  }
}
