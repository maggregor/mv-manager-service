package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.events.Event;
import com.achilio.mvm.service.events.Event.Type;
import com.achilio.mvm.service.events.QueryFetcherJobFinishedEvent;
import java.util.Map;
import org.junit.Test;

public class QueryFetcherJobFinishedEventTest extends EventTest {

  @Override
  protected Event createEvent(String teamName, String projectId) {
    QueryFetcherJobFinishedEvent event = new QueryFetcherJobFinishedEvent(teamName, projectId);
    event.setStatus("completed");
    return event;
  }

  @Test
  public void assertEventType() {
    assertEquals(Type.QUERY_FETCHER_JOB_FINISHED, this.event.getEventType());
  }

  @Test
  public void assertData() {
    assertTrue(event.getData() instanceof Map);
    Map<String, String> data = (Map<String, String>) event.getData();
    assertNotNull(data.get("status"));
    assertEquals("completed", data.get("status"));
  }
}
