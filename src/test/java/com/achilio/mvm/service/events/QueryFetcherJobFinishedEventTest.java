package com.achilio.mvm.service.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.achilio.mvm.service.MockHelper;
import com.achilio.mvm.service.events.Event.Type;
import java.util.Map;
import org.junit.Test;

public class QueryFetcherJobFinishedEventTest extends EventTest {

  @Override
  protected Event createEvent(String teamName, String projectId) {
    return new QueryFetcherJobFinishedEvent(MockHelper.jobExecutionMock(teamName, projectId));
  }

  @Test
  public void assertEventType() {
    assertEquals(Type.QUERY_FETCHER_JOB_FINISHED, this.event.getEventType());
  }

  @Test
  public void assertData() {
    assertNotNull(event.getData());
    Map<String, String> data = event.getData();
    assertNotNull(data.get("status"));
    assertEquals("completed", data.get("status"));
  }
}
