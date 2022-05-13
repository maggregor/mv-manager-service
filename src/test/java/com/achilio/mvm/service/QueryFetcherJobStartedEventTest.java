package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.events.Event;
import com.achilio.mvm.service.events.QueryFetcherJobStartedEvent;
import org.junit.Test;

public class QueryFetcherJobStartedEventTest extends EventTest {

  @Override
  protected Event createEvent(String teamName, String projectId) {
    return new QueryFetcherJobStartedEvent(MockHelper.jobExecutionMock(teamName, projectId));
  }

  @Override
  protected void assertData() {
    assertNull(this.event.getData());
  }

  @Test
  public void assertEventType() {
    assertEquals(Event.Type.QUERY_FETCHER_JOB_STARTED, this.event.getEventType());
  }
}
