package com.achilio.mvm.service.controllers.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAccountConnectionRequest extends ConnectionRequest {

  private String serviceAccount;
}
