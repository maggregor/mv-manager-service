package com.achilio.mvm.service.events;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class EventTest {

  private static final String TEAM_NAME = "achilio.com";
  private static final String PROJECT_ID = "achilio-dev";
  protected Event event;

  protected abstract Event createEvent(String teamName, String projectId);

  protected abstract void assertData();

  protected abstract void assertEventType();

  @Before
  public void setup() {
    event = createEvent(TEAM_NAME, PROJECT_ID);
  }

  @Test
  public void baseGetters() {
    assertEquals(TEAM_NAME, event.getTeamName());
    assertEquals(PROJECT_ID, event.getProjectId());
  }
}
