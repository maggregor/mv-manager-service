package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.responses.OptimizationResponse;
import com.achilio.mvm.service.entities.Optimization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OptimizationResponseTest {

  private final Optimization mockOptimization = mock(Optimization.class);

  @Before
  public void setUp() {
    when(mockOptimization.getId()).thenReturn((long) 1);
    when(mockOptimization.getProjectId()).thenReturn("myProject");
    when(mockOptimization.getQueryEligiblePercentage()).thenReturn(1.0);
    when(mockOptimization.getMvMaxPerTable()).thenReturn(5);
    when(mockOptimization.getMvMaxPlan()).thenReturn(20);
    when(mockOptimization.getMvAppliedCount()).thenReturn(11);
    when(mockOptimization.getMvProposalCount()).thenReturn(15);
    when(mockOptimization.getUsername()).thenReturn("myUser");
  }

  @Test
  public void simpleValidation() {
    OptimizationResponse response = new OptimizationResponse(mockOptimization);
    assertEquals(Long.valueOf(1), response.getId());
    assertEquals("myProject", response.getProjectId());
    assertEquals("myUser", response.getUsername());
    assertEquals(Double.valueOf(1.0), response.getEligiblePercent());
    assertEquals(Integer.valueOf(5), response.getMvMaxPerTable());
    assertEquals(Integer.valueOf(20), response.getMvMaxPlan());
    assertEquals(Integer.valueOf(11), response.getMvAppliedCount());
    assertEquals(Integer.valueOf(15), response.getMvProposalCount());
  }
}
