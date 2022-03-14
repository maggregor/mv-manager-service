package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is an integration tests for the repositories classes
 *
 * All repositories tests must go here, since the context will be instantiated only once
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RepositoriesIntegrationTest {

  private final String TEST_PROJECT_ID1 = "myProjectId";
  private final String TEST_PROJECT_ID2 = "myOtherProjectId";
  private final String table1 = "myTable1";
  private final String table2 = "myTable2";
  private final Set<String> refTables = new HashSet<>(Arrays.asList(table1, table1, table2));
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private final FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final FetcherQueryJob job2 = new FetcherQueryJob(TEST_PROJECT_ID1, 14L);
  private final FetcherQueryJob job3 = new FetcherQueryJob(TEST_PROJECT_ID1, 14L);
  private final FetcherQueryJob job4 = new FetcherQueryJob(TEST_PROJECT_ID2, 14L);

  @Autowired FetcherJobRepository fetcherJobRepository;
  @Autowired QueryRepository queryRepository;

  @Before
  public void setup() {
    job3.setStatus(FetcherJobStatus.FINISHED);
    fetcherJobRepository.save(job1);
    fetcherJobRepository.save(job2);
    fetcherJobRepository.save(job3);
    fetcherJobRepository.save(job4);
    Query query1 =
        new Query(job1, "SELECT 1", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query2 =
        new Query(job1, "SELECT 2", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query3 =
        new Query(job2, "SELECT 2", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query4 =
        new Query(job4, "SELECT 1", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    queryRepository.save(query1);
    queryRepository.save(query2);
    queryRepository.save(query3);
    queryRepository.save(query4);
  }

  @After
  public void cleanUp() {
    queryRepository.deleteAll();
    fetcherJobRepository.deleteAll();
  }

  @Test
  public void saveTest() {
    FetcherQueryJob job = new FetcherQueryJob(TEST_PROJECT_ID2);
    FetcherQueryJob savedJob = fetcherJobRepository.save(job);
    Assert.assertNotNull(savedJob.getCreatedAt());
    Assert.assertFalse(
        fetcherJobRepository.findFetcherQueryJobsByProjectId(TEST_PROJECT_ID2).isEmpty());
  }

  @Test
  public void findAllByProjectIdTest() {
    List<FetcherQueryJob> jobs =
        fetcherJobRepository.findFetcherQueryJobsByProjectId(TEST_PROJECT_ID1);
    Assert.assertEquals(3, jobs.size());
    FetcherJob job = jobs.get(0);
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertNotNull(job.getCreatedAt());
    Assert.assertEquals("PENDING", job.getStatus().toString());
  }

  @Test
  public void findLastTest() {
    Optional<FetcherQueryJob> optionalJob =
        fetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(
            TEST_PROJECT_ID1);
    Assert.assertTrue(optionalJob.isPresent());
    FetcherQueryJob job = optionalJob.get();
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertNotNull(job.getCreatedAt());
    Assert.assertEquals(Long.valueOf(14), job.getTimeframe());
  }

  @Test
  public void findFetcherQueryJobsByProjectIdAndStatusTest() {
    List<FetcherQueryJob> jobs =
        fetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertEquals(2, jobs.size());
  }

  @Test
  public void findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDescTest() {
    Optional<FetcherQueryJob> optionalFetcherJob =
        fetcherJobRepository.findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
            TEST_PROJECT_ID1, FetcherJobStatus.PENDING);
    Assert.assertTrue(optionalFetcherJob.isPresent());

    optionalFetcherJob =
        fetcherJobRepository.findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
            TEST_PROJECT_ID1, FetcherJobStatus.WORKING);
    Assert.assertFalse(optionalFetcherJob.isPresent());
  }

  @Test
  public void findFetcherQueryJobByProjectIdAndIdTest() {
    Optional<FetcherQueryJob> fetchedJob1 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, job1.getId());
    Assert.assertTrue(fetchedJob1.isPresent());
    Assert.assertEquals(job1.getId(), fetchedJob1.get().getId());
    Assert.assertEquals(Long.valueOf(7), fetchedJob1.get().getTimeframe());
    Optional<FetcherQueryJob> fetchedJob2 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, job2.getId());
    Assert.assertTrue(fetchedJob2.isPresent());
    Assert.assertEquals(job2.getId(), fetchedJob2.get().getId());
    Assert.assertEquals(Long.valueOf(14), fetchedJob2.get().getTimeframe());
    Optional<FetcherQueryJob> fetchedJob3 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, 9999L);
    Assert.assertFalse(fetchedJob3.isPresent());
    Optional<FetcherQueryJob> fetchedJob4 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId("projectNotExists", 1L);
    Assert.assertFalse(fetchedJob4.isPresent());
  }

  @Test
  public void findAllQueriesByFetcherQueryJobTest() {
    List<Query> queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job1, TEST_PROJECT_ID1);
    Assert.assertEquals(2, queries.size());

    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job2, TEST_PROJECT_ID1);
    Assert.assertEquals(1, queries.size());

    // Job has no query
    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job3, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    // Job has queries but projectId doesn't match
    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job4, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job4, TEST_PROJECT_ID2);
    Assert.assertEquals(1, queries.size());
  }
}
