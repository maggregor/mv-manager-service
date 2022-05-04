package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.ATable;
import lombok.Getter;

@Getter
public class ATableResponse {

  private final String tableName;
  private final String projectId;
  private final String datasetName;
  private final Double cost;

  public ATableResponse(ATable table) {
    this.tableName = table.getTableName();
    this.projectId = table.getProjectId();
    this.datasetName = table.getDatasetName();
    this.cost = table.getCost();
  }

}
