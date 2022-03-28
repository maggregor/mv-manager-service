package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ConnectionTest {

  private Connection connection;

  protected abstract Connection createConnection(String id, String teamId);

  @Before
  public void setup() {
    connection = createConnection("id-1", "myTeamId");
  }

  @Test
  public void baseGetters() {
    assertEquals("id-1", connection.getId());
    assertEquals("myTeamId", connection.getTeamId());
  }
}
