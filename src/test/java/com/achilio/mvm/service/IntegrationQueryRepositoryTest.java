package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IntegrationQueryRepositoryTest {

  @Autowired
  private QueryRepository repository;

  @AfterEach
  public void cleanUp() {
    repository.deleteAll();
  }

  @Test
  public void findAllByProjectIdAndStartTimeGreaterThanEqual() {
    final String projectId = "theProjectId";
    Query query = new Query();
    query.setId("myId");
    query.setProjectId(projectId);
    query.setStartTime(todayMinusDays(1));
    repository.save(query);
    List<Query> q;
    q = repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, todayMinusDays(1));
    assertEquals(1, q.size());
    assertEquals("myId", q.get(0).getId());
    //
    repository.deleteAll();
    repository.save(simpleQuery(projectId, "id-1", todayMinusDays(10)));
    repository.save(simpleQuery(projectId, "id-2", todayMinusDays(5)));
    repository.save(simpleQuery(projectId, "id-3", todayMinusDays(1)));
    Date from;
    from = todayMinusDays(100);
    q = repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(3, q.size());
    //
    from = todayMinusDays(9);
    q = repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(2, q.size());
    //
    from = todayMinusDays(4);
    q = repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(1, q.size());
    //
    from = todayMinusDays(0);
    q = repository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(0, q.size());
  }

  @Test
  public void findAllByProjectId() {
    final String projectId = "myProject";
    assertEquals(0, repository.findAll().size());
    repository.save(simpleQuery(projectId, "id-1"));
    repository.save(simpleQuery(projectId, "id-2"));
    repository.save(simpleQuery(projectId, "id-3"));
    List<Query> queries = repository.findAllByProjectId(projectId);
    assertEquals(3, queries.size());
    assertEquals("id-1", queries.get(0).getId());
    assertEquals("id-2", queries.get(1).getId());
    assertEquals("id-3", queries.get(2).getId());
  }

  @Test
  public void findQueryByIdAndProjectId() {
    final String projectId = "myProject";
    assertEquals(0, repository.findAll().size());
    repository.save(simpleQuery(projectId, "id-1"));
    repository.save(simpleQuery(projectId, "id-2"));
    repository.save(simpleQuery(projectId, "id-3"));
    Optional<Query> query;
    query = repository.findQueryByIdAndProjectId(projectId, "id-1");
    assertTrue(query.isPresent());
    assertEquals("id-1", query.get().getId());
    query = repository.findQueryByIdAndProjectId(projectId, "id-2");
    assertTrue(query.isPresent());
    assertEquals("id-2", query.get().getId());
    query = repository.findQueryByIdAndProjectId(projectId, "id-3");
    assertTrue(query.isPresent());
    assertEquals("id-3", query.get().getId());
    query = repository.findQueryByIdAndProjectId(projectId, "id-99999");
    assertFalse(query.isPresent());
  }

  @Test
  public void whenSaveTheSameQueryId__thenUpdateQuery() {
    final String projectId = "myProject";
    Query query = simpleQuery(projectId, "id-1");
    query.setQuery("SELECT 1");
    repository.save(query);
    List<Query> queries;
    queries = repository.findAllByProjectId(projectId);
    assertEquals(1, queries.size());
    assertEquals("id-1", queries.get(0).getId());
    assertEquals("SELECT 1", queries.get(0).getQuery());
    query.setQuery("SELECT NEW QUERY");
    repository.save(query);
    queries = repository.findAllByProjectId(projectId);
    assertEquals(1, queries.size());
    assertEquals("id-1", queries.get(0).getId());
    assertEquals("SELECT NEW QUERY", queries.get(0).getQuery());
  }


  private Query simpleQuery(String projectId, String id) {
    return simpleQuery(projectId, id, null);
  }

  private Query simpleQuery(String projectId, String id, Date startTime) {
    Query q = new Query();
    q.setProjectId(projectId);
    q.setId(id);
    q.setStartTime(startTime);
    return q;
  }

  private Date todayMinusDays(int days) {
    ZoneId defaultZoneId = ZoneId.systemDefault();
    LocalDate localDate = LocalDate.now().minusDays(days);
    return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
  }
}
