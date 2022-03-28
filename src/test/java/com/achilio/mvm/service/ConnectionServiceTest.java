package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.ConnectionType;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import com.achilio.mvm.service.services.ConnectionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.logging.log4j.util.Strings;
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
  private final ServiceAccountConnection mockedSAConnection = mock(ServiceAccountConnection.class);
  @InjectMocks private ConnectionService service;
  @Mock private ConnectionRepository mockedRepository;
  private ServiceAccountConnectionRequest mockedSARequest =
      mock(ServiceAccountConnectionRequest.class);
  private Validator validator;

  @Before
  public void setup() {
    when(mockedRepository.save(any())).then(returnsFirstArg());
    when(mockedRepository.findByIdAndTeamName(456L, teamName))
        .thenReturn(Optional.of(mockedSAConnection));
    when(mockedSAConnection.getType()).thenReturn(ConnectionType.SERVICE_ACCOUNT);
    when(mockedSAConnection.getServiceAccount()).thenReturn(JSON_SA_CONTENT);
    //
    mockedSARequest = mock(ServiceAccountConnectionRequest.class);
    when(mockedSARequest.getType()).thenReturn(ConnectionType.SERVICE_ACCOUNT);
    when(mockedSARequest.getServiceAccount()).thenReturn(JSON_SA_CONTENT);
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void createServiceAccountConnection() {
    Connection connection;
    connection = service.createConnection(teamName, mockedSARequest);
    assertExpectedServiceAccount(mockedSARequest, connection);
    // Empty service account
    when(mockedSARequest.getServiceAccount()).thenReturn(Strings.EMPTY);
    connection = service.createConnection(teamName, mockedSARequest);
    Set<ConstraintViolation<Connection>> violations = validator.validate(connection);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Service account must not be empty",
        ((ConstraintViolation<?>) violations.toArray()[0]).getMessage());
  }

  @Test
  public void whenCreateMoreThanOneConnection_thenThrowException() {
    when(mockedRepository.findAllByTeamName(teamName))
        .thenReturn(Collections.singletonList(mockedSAConnection));
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createConnection(teamName, mockedSARequest));
    assertEquals("You cannot create more than one connection per team", e.getMessage());
  }

  @Test
  public void updateServiceAccountConnection() {
    ServiceAccountConnectionRequest updateRequest = mock(ServiceAccountConnectionRequest.class);
    when(updateRequest.getType()).thenReturn(ConnectionType.SERVICE_ACCOUNT);
    when(updateRequest.getServiceAccount()).thenReturn("another_sa_content");
    Connection connection = service.updateConnection(456L, teamName, updateRequest);
    assertExpectedServiceAccount(updateRequest, connection);
    Exception e =
        assertThrows(
            ConnectionNotFoundException.class,
            () -> service.updateConnection(9999L, teamName, mockedSARequest));
    assertEquals("Connection unknownId not found", e.getMessage());
  }

  @Test
  public void deleteConnection() {
    service.deleteConnection(456L, teamName);
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(1))
        .deleteByIdAndTeamName(456L, teamName);
  }

  @Test
  public void getAll() {
    when(mockedRepository.findAllByTeamName(any())).thenReturn(new ArrayList<>());
    assertTrue(service.getAll(teamName).isEmpty());
    when(mockedRepository.findAllByTeamName(any()))
        .thenReturn(Arrays.asList(mockedSAConnection, mockedSAConnection));
    List<Connection> connections = service.getAll(teamName);
    assertEquals(2, connections.size());
    assertEquals(mockedSAConnection, connections.get(0));
    assertEquals(mockedSAConnection, connections.get(1));
  }

  @Test
  public void getServiceAccountConnection() {
    ServiceAccountConnection connection =
        (ServiceAccountConnection) service.getConnection(456L, teamName);
    assertEquals(mockedSAConnection, connection);
  }

  private void assertExpectedServiceAccount(
      ServiceAccountConnectionRequest expected, Connection actual) {
    assertNotNull(actual);
    assertEquals(expected.getType(), actual.getType());
    assertTrue(actual instanceof ServiceAccountConnection);
    String actualServiceAccount = ((ServiceAccountConnection) actual).getServiceAccount();
    assertEquals(mockedSARequest.getServiceAccount(), actualServiceAccount);
  }

  @Test
  public void whenConnectionTypeNull_thenThrowIllegalArgumentException() {
    when(mockedSARequest.getType()).thenReturn(null);
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createConnection(teamName, mockedSARequest));
    assertEquals("Unsupported connection type", e.getMessage());
  }
}
