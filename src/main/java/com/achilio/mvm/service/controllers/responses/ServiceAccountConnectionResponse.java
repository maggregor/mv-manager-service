package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.ServiceAccountConnection;
import lombok.Getter;

@Getter
public class ServiceAccountConnectionResponse extends ConnectionResponse {

  private final String content;

  public ServiceAccountConnectionResponse(ServiceAccountConnection connection) {
    super(connection);
    this.content = "secretkey";
  }
}
