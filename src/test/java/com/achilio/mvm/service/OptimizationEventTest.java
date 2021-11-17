package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationEvent;
import com.achilio.mvm.service.entities.OptimizationEvent.StatusType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OptimizationEventTest {

  private final Optimization mockOptimization = mock(Optimization.class);

  @Test
  public void simpleValidation() {
    OptimizationEvent event;
    event = new OptimizationEvent();
    assertNull(event.getId());
    event = new OptimizationEvent(mockOptimization, StatusType.OPTIMIZING_FIELDS);
    assertEquals(StatusType.OPTIMIZING_FIELDS, event.getStatusType());
    assertEquals(mockOptimization, event.getOptimization());
  }

}
