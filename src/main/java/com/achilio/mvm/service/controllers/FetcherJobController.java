package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.services.FetcherService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/fetcher/job")
@Validated
public class FetcherJobController {
  @Autowired private FetcherService fetcherService;
  @Autowired private FetcherJobRepository fetcherJobRepository;

  @GetMapping(path = "/query/{projectId}", produces = "application/json")
  @ApiOperation("List all fetcher query job for a given projectId")
  public List<FetcherQueryJob> getFetcherQueryJobByProjectId(@PathVariable String projectId) {
    return null;
  }

  @PostMapping(path = "/query/{projectId}", produces = "application/json")
  @ApiOperation("Create and start a new query fetching job")
  public FetcherQueryJob createNewFetcherQueryJob(@PathVariable String projectId) {
    FetcherQueryJob job = new FetcherQueryJob(projectId);
    fetcherJobRepository.save(job);
    return job;
  }

  //  @GetMapping(path = "/project", produces = "application/json")
  //  @ApiOperation("List all projects")
  //  public List<QueryResponse> getQueriesByProject(@PathVariable String projectId) {
  //    return fetcherService.getAllQueriesByProjectId(projectId).stream()
  //        .map(this::toQueryResponse)
  //        .collect(Collectors.toList());
  //  }

}
