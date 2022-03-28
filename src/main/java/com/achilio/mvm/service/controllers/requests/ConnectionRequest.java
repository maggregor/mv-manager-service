package com.achilio.mvm.service.controllers.requests;

import com.achilio.mvm.service.entities.Connection.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ConnectionRequest {

  private ConnectionType type;
}
