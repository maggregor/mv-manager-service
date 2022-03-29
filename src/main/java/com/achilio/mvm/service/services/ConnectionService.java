package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.ConnectionType;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Services to manage connection resources. */
@Service
public class ConnectionService {

  private static final String DEFAULT_CONNECTION_NAME = "Connection to BigQuery";
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);

  private final ConnectionRepository repository;

  public ConnectionService(ConnectionRepository repository) {
    this.repository = repository;
  }

  public List<Connection> getAllConnections(String teamName) {
    return repository.findAllByTeamName(teamName);
  }

  public Optional<Connection> findConnection(Long id, String teamName) {
    return repository.findByIdAndTeamName(id, teamName);
  }

  public Connection getConnection(Long id, String teamName) {
    return findConnection(id, teamName).orElseThrow(() -> new ConnectionNotFoundException(id));
  }

  public void deleteConnection(Long id, String teamName) {
    repository.deleteByIdAndTeamName(id, teamName);
  }

  public Connection createConnection(String teamName, ConnectionRequest request) {
    validateCreate(teamName);
    Connection connection;
    if (ConnectionType.SERVICE_ACCOUNT.equals(request.getType())) {
      ServiceAccountConnectionRequest saRequest = (ServiceAccountConnectionRequest) request;
      connection = new ServiceAccountConnection(saRequest.getContent());
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
    connection.setName(DEFAULT_CONNECTION_NAME);
    connection.setTeamName(teamName);
    return repository.save(connection);
  }

  private void validateCreate(String teamName) {
    if (!getAllConnections(teamName).isEmpty()) {
      throw new IllegalArgumentException("You cannot create more than one connection per team");
    }
  }

  public Connection updateConnection(Long id, String teamName, ConnectionRequest request) {
    Connection connection = getConnection(id, teamName);
    if (ConnectionType.SERVICE_ACCOUNT.equals(connection.getType())) {
      // Update a service account
      ServiceAccountConnection saConnection = (ServiceAccountConnection) connection;
      ServiceAccountConnectionRequest saRequest = (ServiceAccountConnectionRequest) request;
      saConnection.setContent(saRequest.getContent());
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
    LOGGER.info("Connection {} updated", id);
    return repository.save(connection);
  }
}
