package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OptimizationTest {

  @Test
  public void simpleValidation() {
    Project mockedProject = mock(Project.class);
    when(mockedProject.getProjectId()).thenReturn("myProject");
    when(mockedProject.getUsername()).thenReturn("myUser");
    Optimization optimization = new Optimization(mockedProject);
    assertNull(optimization.getId());
    assertNotNull(optimization.getProjectId());
    assertEquals("myProject", optimization.getProjectId());
    assertEquals("myUser", optimization.getUsername());
  }

  @Test
  public void simpleValidationNull() {
    Optimization optimization = new Optimization();
    assertNull(optimization.getId());
    assertNull(optimization.getProjectId());
    assertNull(optimization.getCreatedDate());
  }

  @Test
  public void simpleValidationSetters() {
    Optimization o = new Optimization();
    o.setUsername("myUser");
    assertEquals("myUser", o.getUsername());
    o.setMvAppliedCount(10);
    assertEquals(10, o.getMvAppliedCount());
    o.setMvMaxPerTable(20);
    assertEquals(20, o.getMvMaxPerTable());
    o.setMvProposalCount(40);
    assertEquals(40, o.getMvProposalCount());
    o.setQueryEligiblePercentage(0.95);
    assertEquals(0.95, o.getQueryEligiblePercentage());
    assertNull(o.getId());
    assertNull(o.getProjectId());
    assertNull(o.getCreatedDate());
  }
}
