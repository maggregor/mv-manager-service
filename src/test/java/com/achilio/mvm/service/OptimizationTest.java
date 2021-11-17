package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.achilio.mvm.service.entities.Optimization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OptimizationTest {

  @Test
  public void simpleValidation() {
    Optimization optimization = new Optimization("myProject");
    assertNull(optimization.getId());
    assertNotNull(optimization.getProjectId());
    assertEquals("myProject", optimization.getProjectId());
  }

  @Test
  public void simpleValidationNull() {
    Optimization optimization = new Optimization();
    assertNull(optimization.getId());
    assertNull(optimization.getProjectId());
    assertNull(optimization.getCreatedDate());
  }
}
