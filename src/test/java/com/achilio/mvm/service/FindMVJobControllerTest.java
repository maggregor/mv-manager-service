package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.FindMVJobController;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.Job.JobStatus;
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
  public void getAllMVJob() {
    when(mockedMVJobService.getAllMVJobs(PROJECT_ID, null)).thenReturn(mvJobList1);
    List<FindMVJob> mvJobList = controller.getAllMaterializedViews(PROJECT_ID, null, null);
    assertEquals(2, mvJobList.size());
    assertMVJobEquals(mvJob1, mvJobList.get(0));
    assertMVJobEquals(mvJob2, mvJobList.get(1));
  }

  private void assertMVJobEquals(FindMVJob expectedMVJob, FindMVJob actualMVJob) {
    assertEquals(expectedMVJob.getProjectId(), actualMVJob.getProjectId());
    assertEquals(expectedMVJob.getTimeframe(), actualMVJob.getTimeframe());
    assertEquals(expectedMVJob.getStatus(), actualMVJob.getStatus());
    assertEquals(expectedMVJob.getMvProposalCount(), actualMVJob.getMvProposalCount());
  }
}
