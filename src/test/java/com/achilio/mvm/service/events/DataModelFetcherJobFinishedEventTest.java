package com.achilio.mvm.service.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.MockHelper;
import com.achilio.mvm.service.events.Event.Type;
import org.junit.Test;

public class DataModelFetcherJobFinishedEventTest extends EventTest {

  @Override
  protected Event createEvent(String teamName, String projectId) {
    return new DataModelFetcherJobFinishedEvent(MockHelper.jobExecutionMock(teamName, projectId));
  }

  @Override
  protected void assertData() {
    assertNull(this.event.getData());
  }

  @Test
  public void assertEventType() {
    assertEquals(Type.DATA_MODEL_FETCHER_JOB_FINISHED, this.event.getEventType());
  }
}
