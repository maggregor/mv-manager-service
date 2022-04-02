package com.achilio.mvm.service.controllers.requests;

import com.achilio.mvm.service.entities.Connection.SourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAccountConnectionRequest extends ConnectionRequest {

  private String serviceAccountKey;

  public ServiceAccountConnectionRequest(String name, SourceType sourceType, String serviceAccountKey) {
    this.setName(name);
    this.setSourceType(sourceType);
    this.serviceAccountKey = serviceAccountKey;
  }

  @Override
  public String getContent() {
    return this.serviceAccountKey;
  }
}
