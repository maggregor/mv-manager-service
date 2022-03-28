package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAccountConnectionTest extends ConnectionTest {

  private static final String SERVICE_ACCOUNT_JSON = "json_service_account";

  @Override
  protected Connection createConnection() {
    return new ServiceAccountConnection(SERVICE_ACCOUNT_JSON);
  }

  @Test
  public void getters() {
    ServiceAccountConnection connection;
    connection = new ServiceAccountConnection();
    assertNull(connection.getServiceAccount());
    connection = new ServiceAccountConnection(SERVICE_ACCOUNT_JSON);
    assertEquals(SERVICE_ACCOUNT_JSON, connection.getServiceAccount());
  }
}
