package com.achilio.mvm.service;

import static com.achilio.mvm.service.MockHelper.setupMockedAuthenticationContext;
import static com.achilio.mvm.service.MockHelper.tableMock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ATableController;
import com.achilio.mvm.service.controllers.responses.ATableResponse;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ATableControllerTest {

  private static final String PROJECT_ID = "myProjectId";
  private static final String DATASET_NAME = "myDataset";
  private static final String TABLE_NAME = "myTable";

  @InjectMocks
  private ATableController controller;
  @Mock
  private ProjectService projectService;

  @Before
  public void setup() {
    setupMockedAuthenticationContext();
    ATable table = tableMock(ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME));
    when(table.getCost()).thenReturn(100F);
    when(projectService.getAllTables(PROJECT_ID)).thenReturn(Arrays.asList(table));
  }

  @Test
  public void aTableResponse() {
    List<ATableResponse> actual = controller.getAllTables(PROJECT_ID);
    assertEquals(1, actual.size());
    assertEquals(new Float(100), actual.get(0).getCost());
  }
}
