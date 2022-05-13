package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is an integration tests for the repositories classes
 *
 * <p>All repositories tests must go here, since the context will be instantiated only once
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IntegrationRepositoriesTest {

  private final String TEST_PROJECT_ID1 = "myProjectId";

  private final Connection connection1 = new ServiceAccountConnection("SA_JSON_CONTENT");
  private final Connection connection2 = new ServiceAccountConnection("SA_JSON_CONTENT");

  @Autowired
  private ConnectionRepository connectionRepository;

  @Before
  public void setup() {
    connection1.setTeamName("myTeam");
    connection2.setTeamName("myTeam");
  }

  @BeforeEach
  public void clear() {
    connectionRepository.deleteAll();
  }

  @Test
  public void connection_findAllByTeamName() {
    assertEquals(0, connectionRepository.findAllByTeamName("myTeam").size());
    connectionRepository.save(connection1);
    assertEquals(1, connectionRepository.findAllByTeamName("myTeam").size());
    connectionRepository.save(connection2);
    assertEquals(2, connectionRepository.findAllByTeamName("myTeam").size());
  }

  @Test
  @Transactional
  public void connection_deleteByIdAndTeamName() {
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.save(connection1);
    connectionRepository.save(connection2);
    assertFalse(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection1.getId(), "myTeam");
    assertFalse(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection2.getId(), "myTeam");
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection1.getId(), "myTeam");
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
  }

}
