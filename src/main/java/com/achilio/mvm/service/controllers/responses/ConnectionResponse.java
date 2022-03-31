package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.SourceType;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.LocalDateTime;
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
  private String ownerUsername;
  private LocalDateTime lastModifiedAt;
  private SourceType sourceType;

  public ConnectionResponse(Connection connection) {
    this.id = connection.getId();
    this.name = connection.getName();
    this.ownerUsername = connection.getOwnerUsername();
    this.lastModifiedAt = connection.getLastModifiedAt();
    this.sourceType = connection.getSourceType();
  }
}
