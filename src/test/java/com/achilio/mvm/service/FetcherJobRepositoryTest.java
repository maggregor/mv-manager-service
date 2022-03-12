package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetcherJobRepositoryTest {

  private final String TEST_PROJECT_ID1 = "myProjectId";
  private final String TEST_PROJECT_ID2 = "myOtherProjectId";

  @Autowired FetcherJobRepository fetcherJobRepository;

  @Before
  public void setup() {
    FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
    fetcherJobRepository.save(job1);
    FetcherQueryJob job2 = new FetcherQueryJob(TEST_PROJECT_ID1, 14L);
    fetcherJobRepository.save(job2);
  }

  @Test
  public void saveTest() {
    FetcherQueryJob job = new FetcherQueryJob(TEST_PROJECT_ID2);
    FetcherQueryJob savedJob = fetcherJobRepository.save(job);
    Assert.assertNotNull(savedJob.getCreatedAt());
    Assert.assertFalse(fetcherJobRepository.findAllByProjectId(TEST_PROJECT_ID2).isEmpty());
  }

  @Test
  public void findAllByProjectIdTest() {
    FetcherJob job = fetcherJobRepository.findAllByProjectId(TEST_PROJECT_ID1).get(0);
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertTrue(job instanceof FetcherQueryJob);
    Assert.assertNotNull(job.getCreatedAt());
  }

  @Test
  public void findLastTest() {
    FetcherJob job = fetcherJobRepository.findTopByProjectIdOrderByCreatedAtDesc(TEST_PROJECT_ID1);
    Assert.assertEquals(TEST_PROJECT_ID1, job.getProjectId());
    Assert.assertTrue(job instanceof FetcherQueryJob);
    Assert.assertNotNull(job.getCreatedAt());
    Long expectedTimeframe = 14L;
    Assert.assertEquals(expectedTimeframe, ((FetcherQueryJob) job).getTimeframe());
  }
}
