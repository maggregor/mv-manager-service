package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.TableOptimization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableOptimizationTest {

  @Test
  public void gettersAsserts() {
    TableOptimization tableOptimization = new TableOptimization("myProject", "myDataset",
        "myTable");
    assertEquals("myProject", tableOptimization.getProjectId());
    assertEquals("myDataset", tableOptimization.getDatasetName());
    assertEquals("myTable", tableOptimization.getTableName());
  }

}
