package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.ATable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = Id.NAME, property = "source")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BigQueryTableResponse.class, name = "bigquery"),
})
public class ATableResponse {

  private final String tableName;
  private final String projectId;
  private final String datasetName;
  private final Float cost;

  public ATableResponse(ATable table) {
    this.tableName = table.getTableName();
    this.projectId = table.getProjectId();
    this.datasetName = table.getDatasetName();
    this.cost = table.getCost();
  }

}
