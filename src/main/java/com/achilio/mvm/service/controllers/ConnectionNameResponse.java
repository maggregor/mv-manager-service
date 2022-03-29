package com.achilio.mvm.service.controllers;

import com.achilio.mvm.service.entities.Connection;
import lombok.Getter;

@Getter
public class ConnectionNameResponse {

  private final String name;
  private final Long id;

  public ConnectionNameResponse(Connection connection) {
    this.id = connection.getId();
    this.name = connection.getName();
  }
}
