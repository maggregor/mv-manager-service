package com.achilio.mvm.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.ModelBuilder;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.HashMap;
import org.junit.Test;

public class ModelBuilderTest {

  @Test
  public void isTableRegistered() {
    FetchedTable mockFetchedTable = mock(FetchedTable.class);
    when(mockFetchedTable.getProjectId()).thenReturn("myProject");
    when(mockFetchedTable.getDatasetName()).thenReturn("myDataset");
    when(mockFetchedTable.getTableName()).thenReturn("myTable");
    when(mockFetchedTable.getColumns()).thenReturn(new HashMap<>());
    ModelBuilder builder = new ZetaSQLExtract("myProject");
    builder.registerTable(mockFetchedTable);
    builder.isTableRegistered(mockFetchedTable);
  }

}
