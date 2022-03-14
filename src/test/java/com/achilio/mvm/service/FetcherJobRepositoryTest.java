package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class FetcherJobRepositoryTest {

  private static boolean setUpIsDone = false;
  private final String TEST_PROJECT_ID1 = "fetcherJobRepositoryProjectId";
  private final String TEST_PROJECT_ID2 = "myOtherFetcherJobRepositoryProjectId";
  @Autowired FetcherJobRepository fetcherJobRepository;

  @Before
  public void setup() {
    if (setUpIsDone) {
      return;
    }
    FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
    fetcherJobRepository.save(job1);
    FetcherQueryJob job2 = new FetcherQueryJob(TEST_PROJECT_ID1, 14L);
    fetcherJobRepository.save(job2);
    setUpIsDone = true;
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
    Assert.assertEquals(2, jobs.size());
    FetcherJob job = jobs.get(0);
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertTrue(job instanceof FetcherQueryJob);
    Assert.assertNotNull(job.getCreatedAt());
    Assert.assertEquals("PENDING", job.getStatus().toString());
  }

  @Test
  public void findLastTest() {
    Optional<FetcherQueryJob> optionalJob =
        fetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(
            TEST_PROJECT_ID1);
    Assert.assertTrue(optionalJob.isPresent());
    FetcherJob job = optionalJob.get();
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertTrue(job instanceof FetcherQueryJob);
    Assert.assertNotNull(job.getCreatedAt());
    Assert.assertEquals(Long.valueOf(14), ((FetcherQueryJob) job).getTimeframe());
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
            TEST_PROJECT_ID1, FetcherJobStatus.FINISHED);
    Assert.assertFalse(optionalFetcherJob.isPresent());
  }

  @Test
  public void findFetcherQueryJobByProjectIdAndIdTest() {
    Optional<FetcherQueryJob> fetchedJob1 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, 1L);
    Assert.assertTrue(fetchedJob1.isPresent());
    Assert.assertEquals(Long.valueOf(1), fetchedJob1.get().getId());
    Optional<FetcherQueryJob> fetchedJob2 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, 2L);
    Assert.assertTrue(fetchedJob2.isPresent());
    Assert.assertEquals(Long.valueOf(2), fetchedJob2.get().getId());
    Optional<FetcherQueryJob> job3 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId(TEST_PROJECT_ID1, 99L);
    Assert.assertFalse(job3.isPresent());
    Optional<FetcherQueryJob> job4 =
        fetcherJobRepository.findFetcherQueryJobByProjectIdAndId("projectNotExists", 1L);
    Assert.assertFalse(job4.isPresent());
  }
}
