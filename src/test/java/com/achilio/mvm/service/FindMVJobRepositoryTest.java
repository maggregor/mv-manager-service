package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.repositories.FindMVJobRepository;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class FindMVJobRepositoryTest {

  private static final String PROJECT1 = "myProjectId1";
  private static final String PROJECT2 = "myProjectId2";
  private static final int TIMEFRAME1 = 7;
  private static final int TIMEFRAME2 = 14;
  private static FindMVJob mvJob1;
  private static FindMVJob mvJob2;
  private static FindMVJob mvJob3;

  @Autowired private FindMVJobRepository repository;

  @Before
  public void setup() {
    FindMVJob job1 = new FindMVJob(PROJECT1, TIMEFRAME1);
    FindMVJob job2 = new FindMVJob(PROJECT2, TIMEFRAME2);
    FindMVJob job3 = new FindMVJob(PROJECT1, TIMEFRAME2);
    job1.setStatus(JobStatus.FINISHED);
    mvJob1 = repository.save(job1);
    mvJob2 = repository.save(job2);
    mvJob3 = repository.save(job3);
  }

  @Test
  public void findTopByProjectIdAndStatusOrderByCreatedAtDesc() {
    Optional<FindMVJob> job1 =
        repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(PROJECT1, JobStatus.FINISHED);
    Optional<FindMVJob> job2 =
        repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(PROJECT2, JobStatus.PENDING);
    Optional<FindMVJob> job3 =
        repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(PROJECT1, JobStatus.PENDING);
    Optional<FindMVJob> job4 =
        repository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(PROJECT1, JobStatus.ERROR);
    assertTrue(job1.isPresent());
    assertTrue(job2.isPresent());
    assertTrue(job3.isPresent());
    assertFalse(job4.isPresent());

    assertMVJobEquals(mvJob1, job1.get());
    assertMVJobEquals(mvJob2, job2.get());
    assertMVJobEquals(mvJob3, job3.get());
  }

  @Test
  public void findTopByProjectIdOrderByCreatedAtDesc() {
    Optional<FindMVJob> job1 = repository.findTopByProjectIdOrderByCreatedAtDesc(PROJECT1);
    Optional<FindMVJob> job2 = repository.findTopByProjectIdOrderByCreatedAtDesc(PROJECT2);
    Optional<FindMVJob> job3 = repository.findTopByProjectIdOrderByCreatedAtDesc("unknownProject");
    assertTrue(job1.isPresent());
    assertTrue(job2.isPresent());
    assertFalse(job3.isPresent());

    assertMVJobEquals(mvJob3, job1.get());
    assertMVJobEquals(mvJob2, job2.get());
  }

  @Test
  public void findAllByProjectId() {
    List<FindMVJob> jobList1 = repository.findAllByProjectId(PROJECT1);
    List<FindMVJob> jobList2 = repository.findAllByProjectId(PROJECT2);
    List<FindMVJob> jobList3 = repository.findAllByProjectId("unknownProject");
    assertEquals(2, jobList1.size());
    assertMVJobEquals(mvJob1, jobList1.get(0));
    assertMVJobEquals(mvJob3, jobList1.get(1));

    assertEquals(1, jobList2.size());
    assertMVJobEquals(mvJob2, jobList2.get(0));

    assertEquals(0, jobList3.size());
  }

  @Test
  public void findAllByProjectIdAndStatus() {
    List<FindMVJob> jobList1 = repository.findAllByProjectIdAndStatus(PROJECT1, JobStatus.PENDING);
    List<FindMVJob> jobList2 = repository.findAllByProjectIdAndStatus(PROJECT2, JobStatus.PENDING);
    List<FindMVJob> jobList3 = repository.findAllByProjectIdAndStatus(PROJECT2, JobStatus.FINISHED);
    List<FindMVJob> jobList4 =
        repository.findAllByProjectIdAndStatus("unknownProject", JobStatus.PENDING);
    assertEquals(1, jobList1.size());
    assertMVJobEquals(mvJob3, jobList1.get(0));

    assertEquals(1, jobList2.size());
    assertMVJobEquals(mvJob2, jobList2.get(0));

    assertEquals(0, jobList3.size());
    assertEquals(0, jobList4.size());
  }

  @Test
  public void findMVJob() {
    Optional<FindMVJob> job1 = repository.findByIdAndProjectId(mvJob1.getId(), PROJECT1);
    Optional<FindMVJob> job2 = repository.findByIdAndProjectId(mvJob2.getId(), PROJECT2);
    Optional<FindMVJob> job3 = repository.findByIdAndProjectId(mvJob3.getId(), "unknownProject");

    assertTrue(job1.isPresent());
    assertTrue(job2.isPresent());
    assertFalse(job3.isPresent());
  }

  private void assertMVJobEquals(FindMVJob expectedMVJob, FindMVJob actualMVJob) {
    assertEquals(expectedMVJob.getProjectId(), actualMVJob.getProjectId());
    assertEquals(expectedMVJob.getTimeframe(), actualMVJob.getTimeframe());
    assertEquals(expectedMVJob.getStatus(), actualMVJob.getStatus());
    assertEquals(expectedMVJob.getMvProposalCount(), actualMVJob.getMvProposalCount());
    assertEquals(expectedMVJob.getId(), actualMVJob.getId());
    assertEquals(expectedMVJob.getCreatedAt(), actualMVJob.getCreatedAt());
  }
}
