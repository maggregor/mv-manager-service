package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.MaterializedView;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class MaterializedViewController {

  @GetMapping(path = "/mv")
  @ApiOperation("Get all MV from a project. Can filter by job, dataset, and/or table")
  public MaterializedView getAllMaterializedViews(
      @RequestParam String projectId,
      @RequestParam(required = false) Long jobId,
      @RequestParam(required = false) String datasetName,
      @RequestParam(required = false) String tableName) {
    return null;
  }
}
