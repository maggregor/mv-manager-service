package com.achilio.mvm.service;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.databases.entities.DefaultFetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.services.FetcherJobService;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.ProjectService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetcherJobServiceTest {

  private static final String TEAM_NAME1 = "myTeam1";
  private final String PROJECT_ID = "myProjectId";
  private final String QUERY1 = "SELECT 1";
  private final String QUERY2 = "SELECT 2";
  @InjectMocks FetcherJobService service;
  @Mock FetcherService mockFetcherService;
  @Mock FetcherJobRepository mockFetcherJobRepository;
  @Mock ADatasetRepository mockADatasetRepository;
  @Mock QueryRepository mockQueryRepository;
  @Mock ProjectService mockProjectService;
  @Mock Project mockProject;
  @Mock Connection mockConnection;
  FetcherQueryJobRequest request1 = new FetcherQueryJobRequest(PROJECT_ID, null);
  FetcherQueryJobRequest request2 = new FetcherQueryJobRequest(PROJECT_ID, 14);
  FetcherStructJob structJob1 = new FetcherStructJob();
  FetcherStructJob structJob2 = new FetcherStructJob();
  FetchedQuery fetchedQuery1 = new FetchedQuery(PROJECT_ID, QUERY1);
  FetchedQuery fetchedQuery2 = new FetchedQuery(PROJECT_ID, QUERY2);
  QueryUsageStatistics stats1 = new QueryUsageStatistics(1, 10L, 100L);

  @Before
  public void setup() {
    when(mockFetcherService.fetchQueriesSinceLastDays(any(), any(), anyInt()))
        .thenReturn(Arrays.asList(fetchedQuery1, fetchedQuery2));
    when(mockQueryRepository.saveAll(any())).thenReturn(null);
    when(mockFetcherJobRepository.save(any())).then(returnsFirstArg());
    when(mockProject.getConnection()).thenReturn(mockConnection);
    fetchedQuery1.setStatistics(stats1);
    fetchedQuery2.setStatistics(stats1);
  }

  @Test
  public void fetchAllQueriesJob() throws InterruptedException {
    FetcherQueryJob job = new FetcherQueryJob(PROJECT_ID);
    when(mockProjectService.getProject(any(), any())).thenReturn(mockProject);
    service.fetchAllQueriesJob(job, TEAM_NAME1);
    TimeUnit.SECONDS.sleep(1);
    Assert.assertEquals(FetcherJobStatus.FINISHED, job.getStatus());
    Mockito.verify(mockFetcherJobRepository, Mockito.timeout(1000).times(2))
        .save(ArgumentMatchers.any(FetcherQueryJob.class));
    Mockito.verify(mockQueryRepository, Mockito.timeout(1000).times(1))
        .saveAll(ArgumentMatchers.any());
  }

  @Test
  public void createNewFetcherQueryJob() {
    FetcherQueryJob job1 = service.createNewFetcherQueryJob(PROJECT_ID, request1);
    Assert.assertEquals(PROJECT_ID, job1.getProjectId());
    Assert.assertEquals(7, job1.getTimeframe());

    FetcherQueryJob job2 = service.createNewFetcherQueryJob(PROJECT_ID, request2);
    Assert.assertEquals(PROJECT_ID, job2.getProjectId());
    Assert.assertEquals(14, job2.getTimeframe());
  }

  @Test
  @Ignore
  public void syncDatasets() {
    when(mockProject.getProjectId()).thenReturn(PROJECT_ID);
    when(mockProjectService.getProject(any(), any())).thenReturn(mockProject);
    when(mockProjectService.getProject(any())).thenReturn(mockProject);
    ADataset dataset1 = new ADataset(mockProject, "dataset1");
    ADataset dataset2 = new ADataset(mockProject, "dataset2");
    ADataset dataset3 = new ADataset(mockProject, "dataset3");
    when(mockADatasetRepository.findByProjectAndDatasetName(mockProject, "dataset1"))
        .thenReturn(Optional.of(dataset1));
    when(mockADatasetRepository.findByProjectAndDatasetName(mockProject, "dataset2"))
        .thenReturn(Optional.of(dataset2));
    FetchedDataset fetchedDataset1 =
        new DefaultFetchedDataset(
            PROJECT_ID, "dataset1", "myProjectId:dataset1", null, null, null, null, null);
    FetchedDataset fetchedDataset2 =
        new DefaultFetchedDataset(
            PROJECT_ID, "dataset2", "myProjectId:dataset2", null, null, null, null, null);
    FetchedDataset fetchedDataset3 =
        new DefaultFetchedDataset(
            PROJECT_ID, "dataset3", "myProjectId:dataset3", null, null, null, null, null);
    List<ADataset> localDatasets = new ArrayList<>(Arrays.asList(dataset1, dataset2));
    List<FetchedDataset> allFetchedDatasets =
        new ArrayList<>(Arrays.asList(fetchedDataset2, fetchedDataset3));
    when(mockProjectService.getAllDatasets(any(), any())).thenReturn(localDatasets);
    when(mockFetcherService.fetchAllDatasets(any(), any())).thenReturn(allFetchedDatasets);
    service.syncDatasets(structJob1, TEAM_NAME1);
  }
}
