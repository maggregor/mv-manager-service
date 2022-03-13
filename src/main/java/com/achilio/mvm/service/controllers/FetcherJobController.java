package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.services.FetcherService;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/fetcher/job")
@Validated
public class FetcherJobController {
  @Autowired private FetcherService fetcherService;
  @Autowired private FetcherJobRepository fetcherJobRepository;

  @GetMapping(path = "/query/{projectId}", produces = "application/json")
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public List<FetcherJob> getFetcherQueryJobByProjectId(
      @PathVariable String projectId,
      @RequestParam(required = false) Boolean last,
      @RequestParam(required = false) String status) {
    if (last != null && last) {
      Optional<FetcherJob> optionalFetcherJob;
      if (status != null) {
        optionalFetcherJob =
            fetcherJobRepository.findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
                projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
      } else {
        optionalFetcherJob =
            fetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(projectId);
      }
      return optionalFetcherJob.map(Collections::singletonList).orElse(Collections.EMPTY_LIST);
    }
    if (status != null) {
      return fetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(
          projectId, FetcherJobStatus.valueOf(status.toUpperCase()));
    }
    return fetcherJobRepository.findFetcherQueryJobsByProjectId(projectId);
  }

  @GetMapping(path = "/query/{projectId}/{fetcherQueryJobId}", produces = "application/json")
  @ApiOperation(
      "List all fetcher query job for a given projectId.\n"
          + "If last URL Param is passed and set to true, returns a singleton with the latest fetcher query job")
  public FetcherJob getFetcherQueryJob(
      @PathVariable String projectId, @PathVariable Long fetcherQueryJobId) {

    Optional<FetcherJob> optionalFetcherJob =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(projectId, fetcherQueryJobId);
    if (!optionalFetcherJob.isPresent()) {
      throw new FetcherJobNotFoundException(fetcherQueryJobId.toString());
    }
    return optionalFetcherJob.get();
  }

  @PostMapping(path = "/query/{projectId}", produces = "application/json")
  @ApiOperation("Create and start a new query fetching job")
  public FetcherJob createNewFetcherQueryJob(@PathVariable String projectId) {
    FetcherJob currentJob = fetcherJobRepository.save(new FetcherQueryJob(projectId));
    return currentJob;
  }
}
