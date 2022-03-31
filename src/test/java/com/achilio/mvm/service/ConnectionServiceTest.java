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

  private static final String JSON_SA_CONTENT = "json_content_service_account_xxx";
  private static final String teamName = "myTeam";
  @InjectMocks private ConnectionService service;
  @Mock private ConnectionRepository mockedRepository;
  private ServiceAccountConnection SA_CONNECTION;
  private ServiceAccountConnectionRequest SA_REQUEST;

  private Validator validator;

  @Before
  public void setup() {
    SA_CONNECTION = new ServiceAccountConnection(JSON_SA_CONTENT);
    SA_REQUEST = new ServiceAccountConnectionRequest(JSON_SA_CONTENT);
    when(mockedRepository.save(any())).then(returnsFirstArg());
    when(mockedRepository.findByIdAndTeamName(456L, teamName))
        .thenReturn(Optional.of(SA_CONNECTION));
    //
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void createServiceAccountConnection() {
    Connection connection;
    connection = service.createConnection(teamName, SA_REQUEST);
    assertExpectedServiceAccount(SA_REQUEST, connection);
    // Empty service account
    SA_REQUEST = new ServiceAccountConnectionRequest("");
    connection = service.createConnection(teamName, SA_REQUEST);
    Set<ConstraintViolation<Connection>> violations = validator.validate(connection);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Service account must not be empty",
        ((ConstraintViolation<?>) violations.toArray()[0]).getMessage());
  }

  @Test
  public void updateServiceAccountConnection() {
    ServiceAccountConnectionRequest updateRequest = new ServiceAccountConnectionRequest("another");
    Connection connection = service.updateConnection(456L, teamName, updateRequest);
    assertExpectedServiceAccount(updateRequest, connection);
    Exception e =
        assertThrows(
            ConnectionNotFoundException.class,
            () -> service.updateConnection(9999L, teamName, SA_REQUEST));
    assertEquals("Connection 9999 not found", e.getMessage());
  }

  @Test
  public void deleteConnection() {
    service.deleteConnection(456L, teamName);
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(1)).delete(any());
  }

  @Test
  public void deleteConnection__whenNotExists_throwException() {
    assertThrows(ConnectionNotFoundException.class, () -> service.deleteConnection(789L, teamName));
    assertThrows(
        ConnectionNotFoundException.class, () -> service.deleteConnection(456L, "unknownTeam"));
  }

  @Test
  public void getAll() {
    when(mockedRepository.findAllByTeamName(any())).thenReturn(new ArrayList<>());
    assertTrue(service.getAllConnections(teamName).isEmpty());
    when(mockedRepository.findAllByTeamName(any()))
        .thenReturn(Arrays.asList(SA_CONNECTION, SA_CONNECTION));
    List<Connection> connections = service.getAllConnections(teamName);
    assertEquals(2, connections.size());
    assertEquals(SA_CONNECTION, connections.get(0));
    assertEquals(SA_CONNECTION, connections.get(1));
  }

  @Test
  public void getServiceAccountConnection() {
    ServiceAccountConnection connection =
        (ServiceAccountConnection) service.getConnection(456L, teamName);
    assertEquals(SA_CONNECTION, connection);
  }

  private void assertExpectedServiceAccount(
      ServiceAccountConnectionRequest expected, Connection actual) {
    assertNotNull(actual);
    assertTrue(actual instanceof ServiceAccountConnection);
    String actualServiceAccount = ((ServiceAccountConnection) actual).getServiceAccountKey();
    assertEquals(expected.getServiceAccountKey(), actualServiceAccount);
  }

  @Test
  public void whenConnectionTypeNull_thenThrowIllegalArgumentException() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> service.createConnection(teamName, null));
    assertEquals("Unsupported connection type", e.getMessage());
  }
}
