package com.achilio.mvm.service.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.UserContextHelper;
import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ConnectionResponse;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionResponse;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.services.ConnectionService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ConnectionController {

  @Autowired ConnectionService service;

  @GetMapping(path = "/connection", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public List<ConnectionNameResponse> getAllConnections() {
    return service.getAllConnections(getContextTeamName()).stream()
        .map(ConnectionNameResponse::new)
        .collect(Collectors.toList());
  }

  @PostMapping(path = "/connection", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public Connection createConnection(@RequestBody ConnectionRequest request) {
    return service.createConnection(getContextTeamName(), request);
  }

  @GetMapping(path = "/connection/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get connection")
  public ConnectionResponse getConnection(@PathVariable Long id) {
    return toConnectionResponse(service.getConnection(id, getContextTeamName()));
  }

  private ConnectionResponse toConnectionResponse(Connection connection) {
    if (connection instanceof ServiceAccountConnection) {
      return new ServiceAccountConnectionResponse((ServiceAccountConnection) connection);
    }
    throw new IllegalArgumentException("Unsupported connection response");
  }

  @DeleteMapping(path = "/connection/{:id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public void deleteConnection(@PathVariable Long id) {
    service.deleteConnection(id, getContextTeamName());
  }

  @PatchMapping(path = "/connection/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public Connection updateConnection(
      @PathVariable Long id, @RequestBody ConnectionRequest request) {
    return service.updateConnection(id, getContextTeamName(), request);
  }

  public String getContextTeamName() {
    return UserContextHelper.getUserProfile().getTeamName();
  }
}
