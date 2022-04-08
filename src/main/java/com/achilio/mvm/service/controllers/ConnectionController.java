package com.achilio.mvm.service.controllers;

import static com.achilio.mvm.service.UserContextHelper.getContextTeamName;
import static com.achilio.mvm.service.UserContextHelper.getContextUsername;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.responses.ConnectionResponse;
import com.achilio.mvm.service.controllers.responses.ServiceAccountConnectionResponse;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.services.ConnectionService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class ConnectionController {

  @Autowired ConnectionService service;

  @GetMapping(path = "/connection", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public List<ConnectionResponse> getAllConnections() {
    return service.getAllConnections(getContextTeamName()).stream()
        .map(this::toConnectionResponse)
        .collect(Collectors.toList());
  }

  @PostMapping(path = "/connection", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public ConnectionResponse createConnection(@RequestBody ConnectionRequest request) {
    Connection connection =
        service.createConnection(getContextTeamName(), getContextUsername(), request);
    service.uploadConnectionToGCS(connection);
    return toConnectionResponse(connection);
  }

  @GetMapping(path = "/connection/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Get connection")
  public ConnectionResponse getConnection(@PathVariable Long id) {
    return toConnectionResponse(service.getConnection(id, getContextTeamName()));
  }

  @PatchMapping(path = "/connection/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("Update connection")
  public ConnectionResponse updateConnection(
      @PathVariable Long id, @RequestBody ConnectionRequest request) {
    Connection connection = service.updateConnection(id, getContextTeamName(), request);
    service.uploadConnectionToGCS(connection);
    return toConnectionResponse(connection);
  }

  @DeleteMapping(path = "/connection/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteConnection(@PathVariable Long id) {
    service.deleteConnection(id, getContextTeamName());
  }

  private ConnectionResponse toConnectionResponse(Connection connection) {
    if (connection instanceof ServiceAccountConnection) {
      return new ServiceAccountConnectionResponse((ServiceAccountConnection) connection);
    }
    throw new IllegalArgumentException("Unsupported connection type");
  }
}
