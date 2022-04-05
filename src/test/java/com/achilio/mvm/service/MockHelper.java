package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.Collections;
import java.util.List;

public class MockHelper {

  public static ATable createMockATable(ATableId tableId) {
    return createMockATable(tableId, Collections.emptyList());
  }

  public static ATable createMockATable(ATableId tableId, List<AColumn> columns) {
    ATable mock = mock(ATable.class);
    when(mock.getTableId()).thenReturn(tableId.getTableId());
    when(mock.getProjectId()).thenReturn(tableId.getProject());
    when(mock.getDatasetName()).thenReturn(tableId.getDataset());
    when(mock.getTableName()).thenReturn(tableId.getTable());
    when(mock.getColumns()).thenReturn(columns);
    return mock;
  }
}
