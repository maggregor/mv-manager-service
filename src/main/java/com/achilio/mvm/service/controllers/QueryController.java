package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.services.QueryService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}/fetcher")
@Validated
public class QueryController {

  @Autowired private QueryService queryService;

  @GetMapping(path = "/query/{projectId}/{fetcherQueryJobId}", produces = "application/json")
  @ApiOperation("List all queries for a given projectId and fetcherQueryJobId.\n")
  public List<Query> getAllQueriesByProjectIdAndFetcherQueryJobId(
      @PathVariable String projectId, @PathVariable String fetcherQueryJobId) {
    return queryService.getAllQueriesByJobIdAndProjectId(fetcherQueryJobId, projectId);
  }

  @GetMapping(path = "/query/{projectId}", produces = "application/json")
  @ApiOperation("List all queries for a given projectId and the last fetcherQueryJobId.\n")
  public List<Query> getLastFetcherQueryJobByProjectId(@PathVariable String projectId) {
    return queryService.getAllQueriesByProjectIdLastJob(projectId);
  }
}
