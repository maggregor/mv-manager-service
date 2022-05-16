package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.responses.ATableResponse;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ATableController {

  private final ProjectService projectService;

  public ATableController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping(path = "/table", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all tables")
  public List<ATableResponse> getAllTables(@RequestParam String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    return projectService.getAllTables(projectId).stream().map(ATableResponse::new).collect(
        Collectors.toList());
  }
}
