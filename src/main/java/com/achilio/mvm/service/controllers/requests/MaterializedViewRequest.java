package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MaterializedViewRequest {

  @JsonProperty @NotNull private String projectId;

  @JsonProperty @NotNull private String datasetName;

  @JsonProperty @NotNull private String tableName;

  @JsonProperty @NotNull
  // To Be replaced by a Set of Field in a future version
  private String statement;
}
