package com.achilio.mvm.service.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.services.ConnectionService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
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
  public List<Connection> getAllConnections() {
    return service.getAllConnections(getContextTeamName());
  }

  @PostMapping(path = "/connection", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public Connection createConnection(@RequestBody ConnectionRequest request) {
    return service.createConnection(getContextTeamName(), request);
  }

  @GetMapping(path = "/connection/{:id}", produces = APPLICATION_JSON_VALUE)
  @ApiOperation("List all connection")
  public Connection getConnection(@PathVariable Long id) {
    return service.getConnection(id, getContextTeamName());
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
    return "myTeam";
  }
}
