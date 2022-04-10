package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;

import com.achilio.mvm.service.controllers.requests.MaterializedViewActionRequest;
import com.achilio.mvm.service.controllers.requests.MaterializedViewActionRequest.Action;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.services.MaterializedViewService;
import com.achilio.mvm.service.services.ProjectService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class MaterializedViewController {

  private final MaterializedViewService service;
  private final ProjectService projectService;

  public MaterializedViewController(
      MaterializedViewService service, ProjectService projectService) {
    this.service = service;
    this.projectService = projectService;
  }

  @GetMapping(path = "/mv")
  @ApiOperation("Get all MV from a project. Can filter by job, dataset, and/or table")
  public List<MaterializedView> getAllMaterializedViews(
      @RequestParam String projectId,
      @RequestParam(required = false) String datasetName,
      @RequestParam(required = false) String tableName,
      @RequestParam(required = false) Long jobId) {
    projectService.getProject(projectId, getContextTeamName());
    return service.getAllMaterializedViews(projectId, datasetName, tableName, jobId);
  }

  @GetMapping(path = "/mv/{id}")
  @ApiOperation("Get a single MV from a project")
  public MaterializedView getMaterializedView(
      @PathVariable Long id, @RequestParam String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    return service.getMaterializedView(id);
  }

  @PatchMapping(path = "/mv/{id}")
  @ApiOperation(
      "Use this route to apply or unapply a Materialized View. \n"
          + "If action in the payload is APPLY, this will create it in the BigQuery of"
          + " the project this view belongs to.\n"
          + "If action is UNAPPLY, delete the MV from BigQuery and set the status to NOT_APPLIED."
          + "Does not delete from Achilio DB.\n"
          + "If the MV Id does not exist, returns a 404 NOT FOUND.")
  @ResponseStatus(code = HttpStatus.OK)
  public MaterializedView actionOnMaterializedView(
      @PathVariable Long id, @Valid @RequestBody MaterializedViewActionRequest payload) {
    Project project = projectService.getProject(payload.getProjectId(), getContextTeamName());
    if (payload.getAction().equals(Action.APPLY)) {
      return service.applyMaterializedView(id, project.getConnection());
    }
    return service.unapplyMaterializedView(id, project.getConnection());
  }

  @DeleteMapping(path = "/mv/{id}")
  @ApiOperation(
      "Delete a MV from BigQuery and set the status to NOT_APPLIED. Does not delete from Achilio DB\n"
          + "If the MV Id does not exist, returns a 404 NOT FOUND.")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteMaterializedView(@PathVariable Long id, @RequestParam String projectId) {
    projectService.getProject(projectId, getContextTeamName());
    service.removeMaterializedView(id);
  }
}
