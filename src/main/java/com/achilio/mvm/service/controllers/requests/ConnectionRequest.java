package com.achilio.mvm.service.controllers.requests;

import com.achilio.mvm.service.entities.Connection.SourceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ServiceAccountConnectionRequest.class, name = "service_account"),
})
public abstract class ConnectionRequest {
  @JsonProperty private String name;

  @JsonProperty private SourceType sourceType;

  public abstract String getContent();
}
