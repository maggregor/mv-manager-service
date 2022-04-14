package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.FindMVJobController;
import com.achilio.mvm.service.controllers.requests.FindMVJobRequest;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.FindMVJobNotFoundException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.services.FindMVJobService;
import com.achilio.mvm.service.services.ProjectService;
import java.util.Arrays;
import java.util.List;
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
public class FindMVJobControllerTest {

  private static final String PROJECT_ID = "myProjectId";

  private static final FindMVJob mvJob1 = new FindMVJob(PROJECT_ID, 7);
  private static final FindMVJob mvJob2 = new FindMVJob(PROJECT_ID, 14);
  private static final List<FindMVJob> mvJobList1 = Arrays.asList(mvJob1, mvJob2);
  private static final Project mockProject = mock(Project.class);
  @InjectMocks FindMVJobController controller;
  @Mock private FindMVJobService mockedMVJobService;
  @Mock private ProjectService mockedProjectService;

  @Before
  public void setup() {
    MockHelper.setupMockedAuthenticationContext();
    mvJob1.setStatus(JobStatus.FINISHED);
    mvJob2.setStatus(JobStatus.PENDING);
    mvJob1.setMvProposalCount(4);
    mvJob2.setMvProposalCount(8);
  }

  @Test
  public void getAllMVJobs() {
    when(mockedMVJobService.getAllMVJobs(PROJECT_ID, null)).thenReturn(mvJobList1);
    List<FindMVJob> mvJobList = controller.getAllFindMVJobs(PROJECT_ID, null);
    assertEquals(2, mvJobList.size());
    assertMVJobEquals(mvJob1, mvJobList.get(0));
    assertMVJobEquals(mvJob2, mvJobList.get(1));
  }

  @Test
  public void getAllMVJobs__whenProjectNotFound_throwException() {
    when(mockedProjectService.getProject("unknownProject", "myDefaultTeam"))
        .thenThrow(ProjectNotFoundException.class);
    assertThrows(
        ProjectNotFoundException.class, () -> controller.getAllFindMVJobs("unknownProject", null));
  }

  @Test
  public void getMVJob() {
    when(mockedMVJobService.getMVJob(1L, PROJECT_ID)).thenReturn(mvJob1);
    when(mockedMVJobService.getMVJob(2L, PROJECT_ID)).thenReturn(mvJob2);
    when(mockedMVJobService.getLastMVJobByStatus(PROJECT_ID, JobStatus.PENDING)).thenReturn(mvJob2);
    when(mockedMVJobService.getLastMVJobByStatus(PROJECT_ID, JobStatus.FINISHED))
        .thenReturn(mvJob1);
    when(mockedMVJobService.getLastMVJobByStatus(PROJECT_ID, null)).thenReturn(mvJob1);
    FindMVJob findMVJob1 = controller.getFindMVJob("1", PROJECT_ID, null);
    FindMVJob findMVJob2 = controller.getFindMVJob("2", PROJECT_ID, null);
    FindMVJob findMVJobLast = controller.getFindMVJob("last", PROJECT_ID, null);
    FindMVJob findMVJobLastPending = controller.getFindMVJob("last", PROJECT_ID, JobStatus.PENDING);
    FindMVJob findMVJobLastFinished =
        controller.getFindMVJob("last", PROJECT_ID, JobStatus.FINISHED);
    assertMVJobEquals(mvJob1, findMVJob1);
    assertMVJobEquals(mvJob2, findMVJob2);
    assertMVJobEquals(mvJob1, findMVJobLast);
    assertMVJobEquals(mvJob2, findMVJobLastPending);
    assertMVJobEquals(mvJob1, findMVJobLastFinished);
  }

  @Test
  public void getMVJob__whenProjectNotFound_throwException() {
    when(mockedProjectService.getProject("unknownProject", "myDefaultTeam"))
        .thenThrow(ProjectNotFoundException.class);
    assertThrows(
        ProjectNotFoundException.class, () -> controller.getFindMVJob("1", "unknownProject", null));
    assertThrows(
        ProjectNotFoundException.class,
        () -> controller.getFindMVJob("last", "unknownProject", null));
  }

  @Test
  public void getMVJob__whenMVJobNotFound_throwException() {
    when(mockedMVJobService.getMVJob(3L, PROJECT_ID)).thenThrow(FindMVJobNotFoundException.class);
    when(mockedMVJobService.getLastMVJobByStatus(PROJECT_ID, null))
        .thenThrow(FindMVJobNotFoundException.class);
    assertThrows(
        FindMVJobNotFoundException.class, () -> controller.getFindMVJob("3", PROJECT_ID, null));
    assertThrows(
        FindMVJobNotFoundException.class, () -> controller.getFindMVJob("last", PROJECT_ID, null));
  }

  @Test
  public void startFindMVJob() {
    FindMVJobRequest payload = new FindMVJobRequest(PROJECT_ID);
    when(mockedProjectService.getProject(PROJECT_ID, "myDefaultTeam")).thenReturn(mockProject);
    when(mockProject.getAnalysisTimeframe()).thenReturn(7);
    doNothing().when(mockedMVJobService).startFindMVJob(any(), eq(mockProject));
    when(mockedMVJobService.createMVJob(payload.getProjectId(), 7)).thenReturn(mvJob1);
    when(mockedMVJobService.createMVJob(payload.getProjectId(), 14)).thenReturn(mvJob2);

    FindMVJob createdJob1 = controller.startFindMVJob(payload);
    when(mockProject.getAnalysisTimeframe()).thenReturn(14);
    FindMVJob createdJob2 = controller.startFindMVJob(payload);

    assertMVJobEquals(mvJob1, createdJob1);
    assertMVJobEquals(mvJob2, createdJob2);
  }

  @Test
  public void startFindMVJob__whenProjectNotFound_throwException() {
    when(mockedProjectService.getProject("unknownProject", "myDefaultTeam"))
        .thenThrow(ProjectNotFoundException.class);
    FindMVJobRequest payload = new FindMVJobRequest("unknownProject");
    assertThrows(ProjectNotFoundException.class, () -> controller.startFindMVJob(payload));
  }

  private void assertMVJobEquals(FindMVJob expectedMVJob, FindMVJob actualMVJob) {
    assertEquals(expectedMVJob.getProjectId(), actualMVJob.getProjectId());
    assertEquals(expectedMVJob.getTimeframe(), actualMVJob.getTimeframe());
    assertEquals(expectedMVJob.getStatus(), actualMVJob.getStatus());
    assertEquals(expectedMVJob.getMvProposalCount(), actualMVJob.getMvProposalCount());
  }
}
