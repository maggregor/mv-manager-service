package com.achilio.mvm.service.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.MockHelper;
import com.achilio.mvm.service.events.Event.Type;
import org.junit.Test;

public class DataModelFetcherJobStartedEventTest extends EventTest {

  @Override
  protected Event createEvent(String teamName, String projectId) {
    return new DataModelFetcherJobStartedEvent(MockHelper.jobExecutionMock(teamName, projectId));
  }

  @Override
  protected void assertData() {
    assertNull(this.event.getData());
  }

  @Test
  public void assertEventType() {
    assertEquals(Type.DATA_MODEL_FETCHER_JOB_STARTED, this.event.getEventType());
  }
}
