package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.entities.TableOptimization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableOptimizationTest {

  @Test
  public void constructorWithArguments() {
    TableOptimization tableOptimization = new TableOptimization("myProject", "myDataset",
        "myTable");
    assertEquals("myProject", tableOptimization.getProjectId());
    assertEquals("myDataset", tableOptimization.getDatasetName());
    assertEquals("myTable", tableOptimization.getTableName());
  }

  @Test
  public void constructorEmpty() {
    TableOptimization tableOptimization = new TableOptimization();
    assertNull(tableOptimization.getProjectId());
    assertNull(tableOptimization.getDatasetName());
    assertNull(tableOptimization.getTableName());
  }

}
