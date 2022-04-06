package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.AColumnRepository;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Assert;
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
public class RepositoriesIntegrationTest {

  private final String TEST_PROJECT_ID1 = "myProjectId";
  private final String TEST_PROJECT_ID2 = "myOtherProjectId";
  private final String GOOGLE_JOB_ID1 = "google-id1";
  private final String GOOGLE_JOB_ID2 = "google-id2";
  private final String GOOGLE_JOB_ID3 = "google-id3";
  private final String GOOGLE_JOB_ID4 = "google-id4";
  private final String GOOGLE_JOB_ID5 = "google-id5";
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private final FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final Query query1 =
      new Query(
          job1,
          "SELECT 1",
          GOOGLE_JOB_ID1,
          TEST_PROJECT_ID1,
          "",
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);
  private final Query query2 =
      new Query(
          job1,
          "SELECT 2",
          GOOGLE_JOB_ID2,
          TEST_PROJECT_ID1,
          "",
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);
  private final Query query5 =
      new Query(
          job1,
          "SELECT 1",
          GOOGLE_JOB_ID5,
          TEST_PROJECT_ID1,
          "",
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);
  private final FetcherQueryJob job2 = new FetcherQueryJob(TEST_PROJECT_ID1, 14);
  private final Query query3 =
      new Query(
          job2,
          "SELECT 2",
          GOOGLE_JOB_ID3,
          TEST_PROJECT_ID1,
          "",
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);
  private final FetcherQueryJob job3 = new FetcherQueryJob(TEST_PROJECT_ID1, 14);
  private final FetcherQueryJob job4 = new FetcherQueryJob(TEST_PROJECT_ID2, 14);
  private final Query query4 =
      new Query(
          job4,
          "SELECT 1",
          GOOGLE_JOB_ID4,
          TEST_PROJECT_ID2,
          "",
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);
  private final FetcherStructJob job5 = new FetcherStructJob(TEST_PROJECT_ID1);
  private final Connection connection1 = new ServiceAccountConnection("SA_JSON_CONTENT");
  private final Connection connection2 = new ServiceAccountConnection("SA_JSON_CONTENT");
  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private QueryRepository queryRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ADatasetRepository datasetRepository;
  @Autowired private ATableRepository tableRepository;
  @Autowired private AColumnRepository columnRepository;
  @Autowired private ConnectionRepository connectionRepository;

  @Before
  public void setup() {
    job3.setStatus(FetcherJobStatus.FINISHED);
    fetcherJobRepository.save(job1);
    fetcherJobRepository.save(job2);
    fetcherJobRepository.save(job3);
    fetcherJobRepository.save(job4);
    fetcherJobRepository.save(job5);

    queryRepository.save(query1);
    queryRepository.save(query2);
    queryRepository.save(query3);
    queryRepository.save(query4);
    queryRepository.save(query5);
    Query replacingQuery5 =
        new Query(
            job2,
            "SELECT 2",
            GOOGLE_JOB_ID5,
            TEST_PROJECT_ID1,
            "",
            false,
            false,
            LocalDate.of(2020, 1, 8),
            stats);
    queryRepository.saveAndFlush(replacingQuery5);

    connection1.setTeamName("myTeam");
    connection2.setTeamName("myTeam");
  }

  @After
  public void cleanUp() {
    columnRepository.deleteAll();
    queryRepository.deleteAll();
    fetcherJobRepository.deleteAll();
  }

  @Test
  public void fetcherJob_save() {
    FetcherQueryJob job = new FetcherQueryJob(TEST_PROJECT_ID2);
    FetcherQueryJob savedJob = fetcherJobRepository.save(job);
    Assert.assertNotNull(savedJob.getCreatedAt());
    Assert.assertFalse(
        fetcherJobRepository.findFetcherQueryJobsByProjectId(TEST_PROJECT_ID2).isEmpty());
  }

  @Test
  public void fetcherJob_findAllByProjectId() {
    List<FetcherQueryJob> queryJobs =
        fetcherJobRepository.findFetcherQueryJobsByProjectId(TEST_PROJECT_ID1);
    Assert.assertEquals(3, queryJobs.size());
    FetcherJob job = queryJobs.get(0);
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertNotNull(job.getCreatedAt());
    Assert.assertEquals("PENDING", job.getStatus().toString());

    List<FetcherStructJob> structJobs =
        fetcherJobRepository.findFetcherStructJobsByProjectId(TEST_PROJECT_ID1);
    Assert.assertEquals(1, structJobs.size());

    List<FetcherJob> allJobs = fetcherJobRepository.findFetcherJobsByProjectId(TEST_PROJECT_ID1);
    Assert.assertEquals(4, allJobs.size());
  }

  @Test
  public void fetcherJob_findLastFetcherQueryJob() {
    Optional<FetcherQueryJob> optionalJob =
        fetcherJobRepository.findTopFetcherQueryJobByProjectIdOrderByCreatedAtDesc(
            TEST_PROJECT_ID1);
    List<FetcherQueryJob> allJobs =
        fetcherJobRepository.findFetcherQueryJobsByProjectId(TEST_PROJECT_ID1);
    Assert.assertTrue(optionalJob.isPresent());
    FetcherQueryJob lastJob = allJobs.get(allJobs.size() - 1);
    allJobs.remove(allJobs.size() - 1);
    FetcherQueryJob job = optionalJob.get();
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertNotNull(job.getCreatedAt());
    allJobs.forEach(j -> job.getCreatedAt().isAfter(j.getCreatedAt()));
    job.getCreatedAt().isEqual(lastJob.getCreatedAt());
    Assert.assertEquals(14, job.getTimeframe());
  }

  @Test
  public void fetcherJob_findFetcherQueryJobsByProjectIdAndStatus() {
    List<FetcherQueryJob> queryJobs =
        fetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertEquals(2, queryJobs.size());

    List<FetcherStructJob> structJobs =
        fetcherJobRepository.findFetcherStructJobsByProjectIdAndStatus(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertEquals(1, structJobs.size());

    List<FetcherJob> allJobs =
        fetcherJobRepository.findFetcherJobsByProjectIdAndStatus(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertEquals(3, allJobs.size());
  }

  @Test
  public void fetcherJob_findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc() {
    Optional<FetcherQueryJob> optionalFetcherJob =
        fetcherJobRepository.findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertTrue(optionalFetcherJob.isPresent());

    optionalFetcherJob =
        fetcherJobRepository.findTopFetcherQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
            TEST_PROJECT_ID1, FetcherJobStatus.WORKING);
    Assert.assertFalse(optionalFetcherJob.isPresent());
  }

  @Test
  public void fetcherJob_findFetcherQueryJobByProjectIdAndId() {
    Optional<FetcherQueryJob> fetchedJob1 =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(job1.getId(), TEST_PROJECT_ID1);
    Assert.assertTrue(fetchedJob1.isPresent());
    Assert.assertEquals(job1.getId(), fetchedJob1.get().getId());
    Assert.assertEquals(7, fetchedJob1.get().getTimeframe());
    Optional<FetcherQueryJob> fetchedJob2 =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(job2.getId(), TEST_PROJECT_ID1);
    Assert.assertTrue(fetchedJob2.isPresent());
    Assert.assertEquals(job2.getId(), fetchedJob2.get().getId());
    Assert.assertEquals(14, fetchedJob2.get().getTimeframe());
    Optional<FetcherQueryJob> fetchedJob3 =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(9999L, TEST_PROJECT_ID1);
    Assert.assertFalse(fetchedJob3.isPresent());
    Optional<FetcherQueryJob> fetchedJob4 =
        fetcherJobRepository.findFetcherQueryJobByIdAndProjectId(1L, "projectNotExists");
    Assert.assertFalse(fetchedJob4.isPresent());
  }

  @Test
  public void query_findAllQueriesByFetcherQueryJob() {
    List<Query> queries =
        queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job1, TEST_PROJECT_ID1);
    Assert.assertEquals(3, queries.size());
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID1, q.getProjectId()));
    queries = queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job2, TEST_PROJECT_ID1);
    Assert.assertEquals(1, queries.size());
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID1, q.getProjectId()));

    // Job has no query
    queries = queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job3, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    // Job has queries but projectId doesn't match
    queries = queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job4, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    queries = queryRepository.findAllByInitialFetcherQueryJobAndProjectId(job4, TEST_PROJECT_ID2);
    Assert.assertEquals(1, queries.size());
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID2, q.getProjectId()));
  }

  @Test
  public void query_findFirstByIdAndProjectId() {
    Optional<Query> retrievedQuery1 =
        queryRepository.findQueryByIdAndProjectId(
            query1.getId(), query1.getLastFetcherQueryJob().getProjectId());
    Assert.assertTrue(retrievedQuery1.isPresent());
    Assert.assertEquals("SELECT 1", retrievedQuery1.get().getQuery());
  }

  @Test
  public void query_findAllByLastFetcherQueryJobAndProjectId() {
    List<Query> queries =
        queryRepository.findAllByLastFetcherQueryJobAndProjectId(job4, TEST_PROJECT_ID2);
    Assert.assertEquals(1, queries.size());
  }

  @Test
  public void query_updateQuery() {
    Optional<Query> unchangedQuery =
        queryRepository.findQueryByIdAndProjectId(query5.getId(), TEST_PROJECT_ID1);
    Assert.assertTrue(unchangedQuery.isPresent());
    Query finalQuery = unchangedQuery.get();
    Assert.assertEquals(job2.getId(), finalQuery.getLastFetcherQueryJob().getId());
    Assert.assertEquals(job1.getId(), finalQuery.getInitialFetcherQueryJob().getId());
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

  @Test
  public void column_findAllByProject() {
    String PROJECT_ID1 = "project-id1";
    String PROJECT_ID2 = "project-id2";
    String PROJECT_NAME1 = "Project 1";
    String PROJECT_NAME2 = "Project 2";
    String DATASET_NAME1 = "myDataset1";
    String DATASET_NAME2 = "myDataset2";
    String TABLE_NAME1 = "myTable1";
    String COLUMN_NAME1 = "myColumn1";
    String COLUMN_TYPE1 = "columnType1";

    Project project1 = new Project(PROJECT_ID1, PROJECT_NAME1);
    Project project2 = new Project(PROJECT_ID2, PROJECT_NAME2);
    ADataset dataset1 = new ADataset(project1, DATASET_NAME1);
    ADataset dataset2 = new ADataset(project2, DATASET_NAME2);
    ATable table1 = new ATable(project1, dataset1, TABLE_NAME1);
    ATable table2 = new ATable(project2, dataset1, TABLE_NAME1);
    AColumn column1 = new AColumn(job5, table1, COLUMN_NAME1, COLUMN_TYPE1);
    AColumn column2 = new AColumn(job5, table2, COLUMN_NAME1, COLUMN_TYPE1);
    projectRepository.save(project1);
    projectRepository.save(project2);
    datasetRepository.save(dataset1);
    datasetRepository.save(dataset2);
    tableRepository.save(table1);
    tableRepository.save(table2);
    columnRepository.save(column1);
    columnRepository.save(column2);

    List<AColumn> columns = columnRepository.findAllByTable_Project_ProjectId(PROJECT_ID1);
    assertEquals(1, columns.size());
    assertEquals(
        String.format("%s.%s.%s#%s", PROJECT_ID1, DATASET_NAME1, TABLE_NAME1, COLUMN_NAME1),
        columns.get(0).getColumnId());
  }

  @Test
  public void query_findAllByProjectIdAndStartTimeGreaterThanEqual() {
    final String projectId = "theProjectId";
    Query query = new Query();
    query.setId("myId");
    query.setProjectId(projectId);
    LocalDate date = LocalDate.now();
    query.setStartTime(date);
    queryRepository.save(query);
    List<Query> queries;
    queries = queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, date);
    assertEquals(1, queries.size());
    assertEquals("myId", queries.get(0).getId());
    //
    queryRepository.deleteAll();
    queryRepository.save(simpleQuery(projectId, "id-1", LocalDate.now().minusDays(10)));
    queryRepository.save(simpleQuery(projectId, "id-2", LocalDate.now().minusDays(5)));
    queryRepository.save(simpleQuery(projectId, "id-3", LocalDate.now().minusDays(1)));
    LocalDate from;
    from = LocalDate.now().minusDays(100);
    queries = queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(3, queries.size());
    //
    from = LocalDate.now().minusDays(9);
    queries = queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(2, queries.size());
    //
    from = LocalDate.now().minusDays(4);
    queries = queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(1, queries.size());
    //
    from = LocalDate.now();
    queries = queryRepository.findAllByProjectIdAndStartTimeGreaterThanEqual(projectId, from);
    assertEquals(0, queries.size());
  }

  private Query simpleQuery(String projectId, String id, LocalDate startTime) {
    Query q = new Query();
    q.setProjectId(projectId);
    q.setId(id);
    q.setStartTime(startTime);
    return q;
  }
}
