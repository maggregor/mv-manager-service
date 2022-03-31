package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.SourceType;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import com.achilio.mvm.service.services.ConnectionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceTest {

  private static final String CONNECTION_NAME = "myConnection";
  private static final SourceType SOURCE_TYPE = SourceType.BIGQUERY;
  private static final String OWNER_USERNAME = "myUsername";
  private static final String TEAM_NAME = "myTeam";
  private static final String JSON_SA_CONTENT = "json_content_service_account_xxx";
  private static ServiceAccountConnectionRequest SA_REQUEST;
  private static ServiceAccountConnection SA_CONNECTION;
  @InjectMocks private ConnectionService service;
  @Mock private ConnectionRepository mockedRepository;
  private Validator validator;

  @Before
  public void setup() {
    SA_CONNECTION =
        new ServiceAccountConnection(
            CONNECTION_NAME, TEAM_NAME, OWNER_USERNAME, SourceType.BIGQUERY, JSON_SA_CONTENT);
    SA_REQUEST = new ServiceAccountConnectionRequest(CONNECTION_NAME, SOURCE_TYPE, JSON_SA_CONTENT);
    when(mockedRepository.save(any())).then(returnsFirstArg());
    when(mockedRepository.findByIdAndTeamName(456L, TEAM_NAME))
        .thenReturn(Optional.of(SA_CONNECTION));
    //
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void createServiceAccountConnection() {
    Connection connection;
    connection = service.createConnection(TEAM_NAME, OWNER_USERNAME, SA_REQUEST);
    assertExpectedServiceAccount(SA_CONNECTION, connection);
    // Empty service account
    SA_REQUEST = new ServiceAccountConnectionRequest("");
    connection = service.createConnection(TEAM_NAME, OWNER_USERNAME, SA_REQUEST);
    Set<ConstraintViolation<Connection>> violations = validator.validate(connection);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Service account must not be empty",
        ((ConstraintViolation<?>) violations.toArray()[0]).getMessage());
  }

  @Test
  public void updateServiceAccountConnection() {
    ServiceAccountConnectionRequest updateRequest =
        new ServiceAccountConnectionRequest(CONNECTION_NAME, SOURCE_TYPE, "another");
    Connection connection = service.updateConnection(456L, TEAM_NAME, updateRequest);
    assertExpectedServiceAccount(updateRequest, connection);
    Exception e =
        assertThrows(
            ConnectionNotFoundException.class,
            () -> service.updateConnection(9999L, TEAM_NAME, SA_REQUEST));
    assertEquals("Connection 9999 not found", e.getMessage());
  }

  @Test
  public void deleteConnection() {
    service.deleteConnection(456L, TEAM_NAME);
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(1))
        .deleteByIdAndTeamName(456L, TEAM_NAME);
  }

  @Test
  public void getAll() {
    when(mockedRepository.findAllByTeamName(any())).thenReturn(new ArrayList<>());
    assertTrue(service.getAllConnections(TEAM_NAME).isEmpty());
    when(mockedRepository.findAllByTeamName(any()))
        .thenReturn(Arrays.asList(SA_CONNECTION, SA_CONNECTION));
    List<Connection> connections = service.getAllConnections(TEAM_NAME);
    assertEquals(2, connections.size());
    assertEquals(SA_CONNECTION, connections.get(0));
    assertEquals(SA_CONNECTION, connections.get(1));
  }

  @Test
  public void getServiceAccountConnection() {
    ServiceAccountConnection connection =
        (ServiceAccountConnection) service.getConnection(456L, TEAM_NAME);
    assertEquals(SA_CONNECTION, connection);
  }

  private void assertExpectedServiceAccount(
      ServiceAccountConnectionRequest expected, Connection actual) {
    assertNotNull(actual);
    assertTrue(actual instanceof ServiceAccountConnection);
    String actualServiceAccount = ((ServiceAccountConnection) actual).getServiceAccountKey();
    assertEquals(expected.getServiceAccountKey(), actualServiceAccount);
    assertEquals(expected.getSourceType(), actual.getSourceType());
  }

  private void assertExpectedServiceAccount(ServiceAccountConnection expected, Connection actual) {
    assertNotNull(actual);
    assertTrue(actual instanceof ServiceAccountConnection);
    assertEquals(expected.getServiceAccountKey(), actual.getContent());
    assertEquals(expected.getSourceType(), actual.getSourceType());
    assertEquals(expected.getOwnerUsername(), actual.getOwnerUsername());
  }

  @Test
  public void whenConnectionTypeNull_thenThrowIllegalArgumentException() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createConnection(TEAM_NAME, OWNER_USERNAME, null));
    assertEquals("Unsupported connection type", e.getMessage());
  }
}
