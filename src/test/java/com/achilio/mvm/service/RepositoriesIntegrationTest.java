package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private final FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final Query query1 =
      new Query(
          job1,
          "SELECT 1",
          GOOGLE_JOB_ID1,
          TEST_PROJECT_ID1,
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
          false,
          false,
          LocalDate.of(2020, 1, 8),
          stats);

  @Autowired FetcherJobRepository fetcherJobRepository;
  @Autowired QueryRepository queryRepository;

  @Before
  public void setup() {
    job3.setStatus(FetcherJobStatus.FINISHED);
    fetcherJobRepository.save(job1);
    fetcherJobRepository.save(job2);
    fetcherJobRepository.save(job3);
    fetcherJobRepository.save(job4);

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
    Assert.assertEquals(14, job.getTimeframe());
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
  public void findAllQueriesByFetcherQueryJobTest() {
    List<Query> queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job1, TEST_PROJECT_ID1);
    Assert.assertEquals(2, queries.size());
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID1, q.getProjectId()));
    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job2, TEST_PROJECT_ID1);
    Assert.assertEquals(1, queries.size());
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID1, q.getProjectId()));

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
    queries.forEach(q -> Assert.assertEquals(TEST_PROJECT_ID2, q.getProjectId()));
  }

  @Test
  public void findFirstByIdAndFetcherQueryJob_ProjectIdTest() {
    Optional<Query> retrievedQuery1 =
        queryRepository.findQueryByIdAndFetcherQueryJob_ProjectId(
            query1.getId(), query1.getFetcherQueryJob().getProjectId());
    Assert.assertTrue(retrievedQuery1.isPresent());
    Assert.assertEquals("SELECT 1", retrievedQuery1.get().getQuery());
  }
}
