package com.achilio.mvm.service;

import static com.achilio.mvm.service.MockHelper.connectionMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.ConnectionRequest;
import com.achilio.mvm.service.controllers.requests.ServiceAccountConnectionRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.SourceType;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.exceptions.ConnectionNotFoundException;
import com.achilio.mvm.service.exceptions.InvalidPayloadException;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import com.achilio.mvm.service.services.ConnectionService;
import com.achilio.mvm.service.services.GoogleCloudStorageService;
import java.io.IOException;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceTest {

  private static final String CONNECTION_NAME1 = "myConnection";
  private static final String CONNECTION_NAME2 = "myOtherConnection";
  private static final SourceType SOURCE_BIGQUERY = SourceType.BIGQUERY;
  private static final String OWNER_USERNAME = "myUsername";
  private static final String TEAM_NAME = "myTeam";
  private static final String JSON_SA_CONTENT = "json_content_service_account_xxx";
  private static final String GCS_URL = "gcs://test-bucket/connections/1.json";
  private static ServiceAccountConnectionRequest SA_REQUEST;
  private static ServiceAccountConnection SA_CONNECTION;
  @InjectMocks private ConnectionService service;
  @Mock private ConnectionRepository mockedRepository;
  @Mock private GoogleCloudStorageService mockedStorage;
  private Validator validator;

  @Before
  public void setup() throws IOException {
    SA_CONNECTION =
        new ServiceAccountConnection(
            CONNECTION_NAME1, TEAM_NAME, OWNER_USERNAME, SourceType.BIGQUERY, JSON_SA_CONTENT);
    SA_REQUEST =
        new ServiceAccountConnectionRequest(CONNECTION_NAME1, SOURCE_BIGQUERY, JSON_SA_CONTENT);
    when(mockedRepository.save(any())).then(returnsFirstArg());
    when(mockedRepository.findByIdAndTeamName(456L, TEAM_NAME))
        .thenReturn(Optional.of(SA_CONNECTION));
    //
    when(mockedStorage.uploadObject(any(), any())).thenReturn(GCS_URL);
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void createServiceAccountConnection() {
    Connection connection;
    connection = service.createConnection(TEAM_NAME, OWNER_USERNAME, SA_REQUEST);
    assertExpectedServiceAccount(SA_CONNECTION, connection);
    // Empty service account
    SA_REQUEST = new ServiceAccountConnectionRequest(CONNECTION_NAME1, SOURCE_BIGQUERY, "");
    connection = service.createConnection(TEAM_NAME, OWNER_USERNAME, SA_REQUEST);
    Set<ConstraintViolation<Connection>> violations = validator.validate(connection);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Service account must not be empty",
        ((ConstraintViolation<?>) violations.toArray()[0]).getMessage());
  }

  @Test
  public void createGCSObject() throws IOException {
    Connection connection = connectionMock();
    service.uploadConnectionToGCS(connection);
    Mockito.verify(mockedStorage, Mockito.timeout(1000).times(1)).uploadObject(any(), any());
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(1)).save(any());
    when(mockedStorage.uploadObject(any(), any())).thenThrow(new IOException());
    assertThrows(RuntimeException.class, () -> service.uploadConnectionToGCS(connection));
  }

  @Test
  public void createServiceAccountConnection__whenSourceTypeNull_throwException() {
    SA_REQUEST = new ServiceAccountConnectionRequest(CONNECTION_NAME1, null, JSON_SA_CONTENT);
    Assert.assertThrows(
        InvalidPayloadException.class,
        () -> service.createConnection(TEAM_NAME, OWNER_USERNAME, SA_REQUEST));
  }

  @Test
  public void updateServiceAccountConnection() {
    ServiceAccountConnectionRequest updateRequest =
        new ServiceAccountConnectionRequest(CONNECTION_NAME1, SOURCE_BIGQUERY, "another");
    Connection connection = SA_CONNECTION;
    connection = service.updateConnection(456L, TEAM_NAME, updateRequest);
    assertExpectedServiceAccount(updateRequest, connection);
    Exception e =
        assertThrows(
            ConnectionNotFoundException.class,
            () -> service.updateConnection(9999L, TEAM_NAME, SA_REQUEST));
    assertEquals("Connection 9999 not found", e.getMessage());
  }

  @Test
  public void updateServiceAccountConnection__whenContentEmpty_updateAllButContent() {
    ServiceAccountConnectionRequest updateRequest =
        new ServiceAccountConnectionRequest(CONNECTION_NAME2, SOURCE_BIGQUERY, "");
    ServiceAccountConnection expected =
        new ServiceAccountConnection(
            CONNECTION_NAME2, TEAM_NAME, OWNER_USERNAME, SOURCE_BIGQUERY, JSON_SA_CONTENT);
    Connection connection = service.updateConnection(456L, TEAM_NAME, updateRequest);
    assertExpectedServiceAccount(expected, connection);
  }

  @Test
  public void deleteConnection() {
    Project project1 = new Project("projectId1");
    SA_CONNECTION.setProjects(Collections.singletonList(project1));
    Assert.assertThrows(
        IllegalArgumentException.class, () -> service.deleteConnection(456L, TEAM_NAME));
    SA_CONNECTION.setProjects(Collections.emptyList());
    service.deleteConnection(456L, TEAM_NAME);
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(1)).delete(SA_CONNECTION);
    Mockito.verify(mockedStorage, Mockito.timeout(1000).times(1)).deleteObject(any());
  }

  @Test
  public void deleteConnection__whenNotFound_doNothing() {
    when(mockedRepository.findByIdAndTeamName(789L, TEAM_NAME)).thenReturn(Optional.empty());
    service.deleteConnection(789L, TEAM_NAME);
    Mockito.verify(mockedRepository, Mockito.timeout(1000).times(0)).delete(any());
    Mockito.verify(mockedStorage, Mockito.timeout(1000).times(0)).deleteObject(any());
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

  /** TODO: How can we make a ConnectionType null ? */
  @Test
  @Ignore
  public void whenConnectionTypeNull_thenThrowIllegalArgumentException() {
    ConnectionRequest request = SA_REQUEST;
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.createConnection(TEAM_NAME, OWNER_USERNAME, request));
    assertEquals("Unsupported connection type", e.getMessage());
  }
}
