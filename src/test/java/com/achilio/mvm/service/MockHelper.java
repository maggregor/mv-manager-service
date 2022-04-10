package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockHelper {

  private static final String DEFAULT_PROJECT_ID = "myproject";

  public static Project projectMock(String projectId, Connection connection) {
    Project mock = mock(Project.class);
    //    when(mock.getProjectId()).thenReturn(projectId);
    when(mock.getConnection()).thenReturn(connection);
    return mock;
  }

  public static ADataset datasetMock(Project project, String datasetName) {
    ADataset mock = mock(ADataset.class);
    when(mock.getProject()).thenReturn(project);
    when(mock.getDatasetName()).thenReturn(datasetName).thenReturn(datasetName);
    when(mock.getDatasetId()).thenReturn(project.getProjectId() + "." + datasetName);
    return mock;
  }

  public static ATable tableMock(ATableId tableId) {
    return tableMock(tableId, Collections.emptyList());
  }

  public static ATable tableMock(ATableId tableId, List<AColumn> columns) {
    ATable mock = mock(ATable.class);
    when(mock.getProjectId()).thenReturn(tableId.getProject());
    when(mock.getDatasetName()).thenReturn(tableId.getDataset());
    when(mock.getTableName()).thenReturn(tableId.getTable());
    when(mock.getColumns()).thenReturn(columns);
    return mock;
  }

  public static AColumn columnMock(String name, String type) {
    AColumn column = mock(AColumn.class);
    when(column.getName()).thenReturn(name);
    when(column.getType()).thenReturn(type);
    return column;
  }

  public static Query queryMock(String statement) {
    return queryMock(DEFAULT_PROJECT_ID, statement);
  }

  public static Query queryMock(String projectId, String statement) {
    Query mockedQuery = mock(Query.class);
    when(mockedQuery.getProjectId()).thenReturn(projectId);
    when(mockedQuery.getQuery()).thenReturn(statement);
    return mockedQuery;
  }

  public static FindMVJob findMVJobMock() {
    FindMVJob job = mock(FindMVJob.class);
    //    when(job.getProjectId()).thenReturn("project1");
    //    when(job.getId()).thenReturn(1L);
    return job;
  }

  public static MaterializedView mvMock(
      Long id, String projectId, String datasetName, String tableName, String statement) {
    String hashedStatement = String.valueOf(Math.abs(statement.hashCode()));
    MaterializedView mockedMV = mock(MaterializedView.class);
    when(mockedMV.getId()).thenReturn(id);
    when(mockedMV.getProjectId()).thenReturn(projectId);
    when(mockedMV.getDatasetName()).thenReturn(datasetName);
    when(mockedMV.getTableName()).thenReturn(tableName);
    when(mockedMV.getStatus()).thenReturn(MVStatus.NOT_APPLIED);
    when(mockedMV.getStatement()).thenReturn(statement);
    when(mockedMV.getStatementHashCode()).thenReturn(hashedStatement);
    when(mockedMV.getMvName())
        .thenReturn(String.join("_", tableName, "achilio_mv_", hashedStatement));
    when(mockedMV.getMvUniqueName())
        .thenReturn(String.join("-", projectId, datasetName, tableName, hashedStatement));

    return mockedMV;
  }

  public static void setupMockedAuthenticationContext() {
    setupMockedAuthenticationContext("myDefaultTeam");
  }

  public static void setupMockedAuthenticationContext(String teamName) {
    Authentication mockedAuth = mock(Authentication.class);
    SecurityContext mockedSecurityContext = mock(SecurityContext.class);
    UserProfile mockedUserProfile = mock(UserProfile.class);
    when(mockedUserProfile.getTeamName()).thenReturn(teamName);
    when(mockedAuth.getDetails()).thenReturn(mockedUserProfile);
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
  }

  public static Connection connectionMock() {
    Connection connection = mock(Connection.class);
    //    when(connection.getContent()).thenReturn("serviceAccountJson");
    //    when(connection.getId()).thenReturn(1L);
    //    when(connection.getTeamName()).thenReturn("achilio.com");
    return connection;
  }
}
