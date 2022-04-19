package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.controllers.requests.QueryRequest;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.services.QueryService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class QueryController {

  private final QueryService service;

  public QueryController(QueryService queryService) {
    this.service = queryService;
  }

  @GetMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("List all queries for a given projectId.\n")
  public List<Query> getAllQueriesByProjectId(@RequestParam String projectId) {
    return service.getAllQueries(projectId);
  }

  @GetMapping(path = "/query/{queryId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Retrieve a single query by projectId and queryId")
  public Query getSingleQuery(@RequestParam String projectId, @PathVariable String queryId) {
    return service.getQuery(projectId, queryId);
  }

  @GetMapping(path = "/query/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Retrieve a single query by projectId and queryId")
  public Long getStatistics(@RequestParam String projectId, @RequestParam int timeframe,
      @RequestParam String type) {
    switch (type) {
      case "total_queries":
        return service.getTotalQuerySince(projectId, timeframe);
      case "average_processed_bytes":
        return service.getAverageProcessedBytesSince(projectId, timeframe);
      case "percent_query_in_mv":
        return service.getPercentQueryInMVSince(projectId, timeframe);
      default:
        throw new IllegalArgumentException("Unknown statistics type");
    }
  }


  @PostMapping(path = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Register a new query")
  @PreAuthorize("hasRole('ADMIN')")
  public Query createQuery(@Valid @RequestBody QueryRequest payload) {
    return new Query(payload.getQuery(), payload.getProjectId());
  }
}
