package com.achilio.mvm.service.controllers.requests;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ServiceAccountConnection.class, name = "service_account"),
})
public abstract class ConnectionResponse {

  private Long id;
  private String name;

  public ConnectionResponse(Connection connection) {
    this.id = connection.getId();
    this.name = connection.getName();
  }
}
