package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Connection.ConnectionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class ConnectionTest {

  private Connection connection;

  protected abstract Connection createConnection();

  @Before
  public void setup() {
    connection = createConnection();
    connection.setTeamName("myTeam");
    connection.setId(123L);
  }

  @Test
  public void baseGetters() {
    assertEquals(123L, connection.getId().longValue());
    assertEquals("myTeam", connection.getTeamName());
    assertEquals(ConnectionType.SERVICE_ACCOUNT, connection.getType());
    assertNull(connection.getConnectionFileUrl());
  }
}
