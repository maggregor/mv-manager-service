package com.achilio.mvm.service.services;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.exceptions.InvalidPayloadException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Services to manage connection resources. */
@Service
public class ConnectionService {

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

  @Transactional
  public void deleteConnection(Long id, String teamName) {
    repository.deleteByIdAndTeamName(id, teamName);
  }

  @Transactional
  public Connection createConnection(
      String teamName, String ownerUsername, ConnectionRequest request) {
    Connection connection;
    if (request instanceof ServiceAccountConnectionRequest) {
      connection = new ServiceAccountConnection(request.getContent());
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
    connection.setName(request.getName());
    connection.setTeamName(teamName);
    connection.setOwnerUsername(ownerUsername);
    connection.setSourceType(request.getSourceType());
    if (request.getSourceType() == null) {
      throw new InvalidPayloadException();
    }
    return repository.save(connection);
  }

  public Connection updateConnection(Long id, String teamName, ConnectionRequest request) {
    if (request instanceof ServiceAccountConnectionRequest) {
      // Update a service account
      Connection connection = getConnection(id, teamName);
      connection.setName(request.getName());
      connection.setContent(request.getContent());
      LOGGER.info("Connection {} updated", id);
      return repository.save(connection);
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
  }
}
