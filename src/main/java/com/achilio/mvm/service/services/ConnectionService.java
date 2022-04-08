package com.achilio.mvm.service.services;

import static com.achilio.mvm.service.utils.GoogleCloudStorage.uploadObject;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionInUseException;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.exceptions.InvalidPayloadException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Services to manage connection resources. */
@Service
public class ConnectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionService.class);

  private final ConnectionRepository repository;
  private final String projectId;
  private final String bucketName;

  public ConnectionService(
      ConnectionRepository repository,
      @Value("${publisher.google-project-id}") String projectId,
      @Value("${connection.bucket.name}") String bucketName) {
    this.repository = repository;
    this.projectId = projectId;
    this.bucketName = bucketName;
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
    Optional<Connection> optionalConnection = repository.findByIdAndTeamName(id, teamName);
    if (optionalConnection.isPresent()) {
      Connection connection = optionalConnection.get();
      if (connection.getProjects().size() != 0) {
        String errorMessage =
            String.format(
                "Connection %s is used by one or more projects and cannot be deleted",
                connection.getName());
        LOGGER.warn(errorMessage);
        throw new ConnectionInUseException(errorMessage);
      }
      repository.delete(connection);
    }
  }

  @Transactional
  public Connection createConnection(
      String teamName, String ownerUsername, ConnectionRequest request) throws IOException {
    Connection connection;
    if (request.getSourceType() == null) {
      throw new InvalidPayloadException();
    }
    if (request instanceof ServiceAccountConnectionRequest) {
      connection = new ServiceAccountConnection(request.getContent());
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
    connection.setConnectionFileUrl(uploadConnection(projectId, bucketName, connection));
    connection.setName(request.getName());
    connection.setTeamName(teamName);
    connection.setOwnerUsername(ownerUsername);
    connection.setSourceType(request.getSourceType());
    return repository.save(connection);
  }

  private String uploadConnection(String projectId, String bucketName, Connection connection)
      throws IOException {
    String objectName = connection.getTeamName() + "/" + connection.getId() + ".json";
    return uploadObject(projectId, bucketName, objectName, connection.getContent());
  }

  public Connection updateConnection(Long id, String teamName, ConnectionRequest request)
      throws IOException {
    if (request instanceof ServiceAccountConnectionRequest) {
      // Update a service account
      Connection connection = getConnection(id, teamName);
      connection.setName(request.getName());
      if (!request.getContent().isEmpty()) {
        connection.setContent(request.getContent());
        connection.setConnectionFileUrl(uploadConnection(projectId, bucketName, connection));
      }
      LOGGER.info("Connection {} updated", id);
      return repository.save(connection);
    } else {
      throw new IllegalArgumentException("Unsupported connection type");
    }
  }
}
