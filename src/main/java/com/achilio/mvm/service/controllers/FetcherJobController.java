package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.services.FetcherJobService;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired private FetcherJobService fetcherJobService;
  @Autowired private FetcherJobRepository fetcherJobRepository;

  // Fetcher for Queries

  @GetMapping(path = "/query/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public List<FetcherQueryJob> getAllFetcherQueryJobsByProjectId(
      @PathVariable String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) String status) {
    if (last != null && last) {
      Optional<FetcherQueryJob> optionalFetcherJob;
      if (status != null) {
        optionalFetcherJob =
            fetcherJobService.getLastFetcherQueryJob(
                projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
      } else {
        optionalFetcherJob = fetcherJobService.getLastFetcherQueryJob(projectId);
      }
      return optionalFetcherJob.map(Collections::singletonList).orElse(Collections.EMPTY_LIST);
    }
    if (status != null) {
      return fetcherJobService.getAllQueryJobs(
          projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
    }
    return fetcherJobService.getAllQueryJobs(projectId);
  }

  @GetMapping(
      path = "/query/{projectId}/{fetcherQueryJobId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public FetcherQueryJob getFetcherQueryJob(
      @PathVariable String projectId, @PathVariable Long fetcherQueryJobId) {

    Optional<FetcherQueryJob> optionalFetcherJob =
        fetcherJobService.getFetcherQueryJob(fetcherQueryJobId, projectId);
    if (!optionalFetcherJob.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherQueryJobId.toString());
    }
    return optionalFetcherJob.get();
  }

  @PostMapping(path = "/query/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public FetcherQueryJob createNewFetcherQueryJob(
      @PathVariable String projectId, @RequestBody FetcherQueryJobRequest payload) {
    FetcherQueryJob currentJob = fetcherJobService.createNewFetcherQueryJob(projectId, payload);
    LOGGER.info("Starting FetcherQueryJob {}", currentJob.getId());
    fetcherJobService.fetchAllQueriesJob(currentJob, getContextTeamName());
    return currentJob;
  }

  // Fetcher for Structs

  @GetMapping(path = "/struct/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public List<FetcherStructJob> getAllFetcherStructJobsByProjectId(
      @PathVariable String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) String status) {
    if (last != null && last) {
      Optional<FetcherStructJob> optionalFetcherJob;
      if (status != null) {
        optionalFetcherJob =
            fetcherJobService.getLastFetcherStructJob(
                projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
      } else {
        optionalFetcherJob = fetcherJobService.getLastFetcherStructJob(projectId);
      }
      return optionalFetcherJob.map(Collections::singletonList).orElse(Collections.EMPTY_LIST);
    }
    if (status != null) {
      return fetcherJobService.getAllStructJobs(
          projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
    }
    return fetcherJobService.getAllStructJobs(projectId);
  }

  @GetMapping(
      path = "/struct/{projectId}/{fetcherQueryJobId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public FetcherStructJob getFetcherStructJob(
      @PathVariable String projectId, @PathVariable Long fetcherQueryJobId) {

    Optional<FetcherStructJob> optionalFetcherJob =
        fetcherJobService.getFetcherStructJob(fetcherQueryJobId, projectId);
    if (!optionalFetcherJob.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherQueryJobId.toString());
    }
    return optionalFetcherJob.get();
  }

  @PostMapping(path = "/struct/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Create and start a new query fetching job")
  public FetcherStructJob createNewFetcherStructJob(@PathVariable String projectId) {
    FetcherStructJob currentJob = fetcherJobService.createNewFetcherStructJob(projectId);
    LOGGER.info("Starting FetcherQueryJob {}", currentJob.getId());
    fetcherJobService.fetchAllStructsJob(currentJob, getContextTeamName());
    return currentJob;
  }
}
