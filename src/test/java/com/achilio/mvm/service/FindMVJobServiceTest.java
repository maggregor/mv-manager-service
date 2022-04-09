package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.exceptions.FindMVJobNotFoundException;
import com.achilio.mvm.service.repositories.FindMVJobRepository;
import com.achilio.mvm.service.services.FindMVJobService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FindMVJobServiceTest {

  private static final String PROJECT_ID = "myProjectId";
  private static final FindMVJob mvJob1 = new FindMVJob(PROJECT_ID, 7);
  private static final FindMVJob mvJob2 = new FindMVJob(PROJECT_ID, 14);
  private static final List<FindMVJob> mvJobList1 = Arrays.asList(mvJob1, mvJob2);

  @InjectMocks FindMVJobService service;
  @Mock FindMVJobRepository findMVJobRepository;

  @Before
  public void setup() {
    when(findMVJobRepository.findAllByProjectId(PROJECT_ID)).thenReturn(mvJobList1);
    when(findMVJobRepository.findAllByProjectIdAndStatus(PROJECT_ID, JobStatus.PENDING))
        .thenReturn(Collections.singletonList(mvJob2));
    when(findMVJobRepository.findAllByProjectIdAndStatus(PROJECT_ID, JobStatus.FINISHED))
        .thenReturn(Collections.singletonList(mvJob1));
    when(findMVJobRepository.findAllByProjectIdAndStatus(PROJECT_ID, JobStatus.ERROR))
        .thenReturn(Collections.emptyList());
    when(findMVJobRepository.findByIdAndProjectId(1L, PROJECT_ID)).thenReturn(Optional.of(mvJob1));
    when(findMVJobRepository.findByIdAndProjectId(2L, PROJECT_ID)).thenReturn(Optional.of(mvJob2));
    when(findMVJobRepository.findByIdAndProjectId(3L, PROJECT_ID)).thenReturn(Optional.empty());
    when(findMVJobRepository.findByIdAndProjectId(2L, "unknownProject"))
        .thenReturn(Optional.empty());
    when(findMVJobRepository.findTopByProjectIdOrderByCreatedAtDesc(PROJECT_ID))
        .thenReturn(Optional.of(mvJob2));
    when(findMVJobRepository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(
            PROJECT_ID, JobStatus.PENDING))
        .thenReturn(Optional.of(mvJob2));
    when(findMVJobRepository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(
            PROJECT_ID, JobStatus.FINISHED))
        .thenReturn(Optional.of(mvJob1));
    when(findMVJobRepository.findTopByProjectIdAndStatusOrderByCreatedAtDesc(
            PROJECT_ID, JobStatus.ERROR))
        .thenReturn(Optional.empty());
    when(findMVJobRepository.save(any())).then(returnsFirstArg());
  }

  @Test
  public void getAllMVJobs_NoStatus() {
    List<FindMVJob> mvJobs = service.getAllMVJobs(PROJECT_ID, null);
    assertEquals(2, mvJobs.size());
    assertMVJobEquals(mvJob1, mvJobs.get(0));
    assertMVJobEquals(mvJob2, mvJobs.get(1));
  }

  @Test
  public void getAllMVJobs_WithStatus() {
    List<FindMVJob> mvJobs1 = service.getAllMVJobs(PROJECT_ID, JobStatus.FINISHED);
    assertEquals(1, mvJobs1.size());
    assertMVJobEquals(mvJob1, mvJobs1.get(0));

    List<FindMVJob> mvJobs2 = service.getAllMVJobs(PROJECT_ID, JobStatus.PENDING);
    assertEquals(1, mvJobs2.size());
    assertMVJobEquals(mvJob2, mvJobs2.get(0));

    List<FindMVJob> mvJobs3 = service.getAllMVJobs(PROJECT_ID, JobStatus.ERROR);
    assertEquals(0, mvJobs3.size());
  }

  @Test
  public void getMVJob() {
    FindMVJob job1 = service.getMVJob(1L, PROJECT_ID);
    FindMVJob job2 = service.getMVJob(2L, PROJECT_ID);

    assertMVJobEquals(mvJob1, job1);
    assertMVJobEquals(mvJob2, job2);
  }

  @Test
  public void getMVJob__whenProjectOrIdUnknown_throwException() {
    assertThrows(FindMVJobNotFoundException.class, () -> service.getMVJob(3L, PROJECT_ID));
    assertThrows(FindMVJobNotFoundException.class, () -> service.getMVJob(2L, "unknownProject"));
  }

  @Test
  public void getLastMVJobByStatus() {
    FindMVJob job1 = service.getLastMVJobByStatus(PROJECT_ID, null);
    FindMVJob job2 = service.getLastMVJobByStatus(PROJECT_ID, JobStatus.PENDING);
    FindMVJob job3 = service.getLastMVJobByStatus(PROJECT_ID, JobStatus.FINISHED);

    assertMVJobEquals(mvJob2, job1);
    assertMVJobEquals(mvJob2, job2);
    assertMVJobEquals(mvJob1, job3);
  }

  @Test
  public void getLastMVJobByStatus__whenNoJob_throwException() {
    assertThrows(
        FindMVJobNotFoundException.class,
        () -> service.getLastMVJobByStatus(PROJECT_ID, JobStatus.ERROR));
  }

  @Test
  public void createMVJob() {
    FindMVJob job1 = service.createMVJob(PROJECT_ID, 7);
    FindMVJob job2 = service.createMVJob(PROJECT_ID, 14);

    assertMVJobEquals(mvJob1, job1);
    assertMVJobEquals(mvJob2, job2);
  }

  private void assertMVJobEquals(FindMVJob expectedMVJob, FindMVJob actualMVJob) {
    assertEquals(expectedMVJob.getProjectId(), actualMVJob.getProjectId());
    assertEquals(expectedMVJob.getTimeframe(), actualMVJob.getTimeframe());
    assertEquals(expectedMVJob.getStatus(), actualMVJob.getStatus());
    assertEquals(expectedMVJob.getMvProposalCount(), actualMVJob.getMvProposalCount());
  }
}
