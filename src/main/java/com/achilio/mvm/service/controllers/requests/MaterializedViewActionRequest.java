package com.achilio.mvm.service.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MaterializedViewActionRequest {

  @JsonProperty @NotNull private String projectId;

  @JsonProperty @NotNull private Action action;

  public enum Action {
    APPLY,
    UNAPPLY
  }
}
