package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.Collections;
import java.util.List;

public class MockHelper {

  private static final String DEFAULT_PROJECT_ID = "myproject";

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
}
