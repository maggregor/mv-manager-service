package com.achilio.mvm.service;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.services.FetcherJobService;
import com.achilio.mvm.service.services.FetcherService;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetcherJobServiceTest {

  private final String PROJECT_ID = "myProjectId";
  private final String QUERY1 = "SELECT 1";
  private final String QUERY2 = "SELECT 2";
  @InjectMocks FetcherJobService service;
  @Mock FetcherService fetcherService;
  @Mock FetcherJobRepository mockFetcherJobRepository;
  @Mock QueryRepository mockQueryRepository;
  FetcherQueryJobRequest request1 = new FetcherQueryJobRequest(null);
  FetcherQueryJobRequest request2 = new FetcherQueryJobRequest(14);
  FetchedQuery fetchedQuery1 = new FetchedQuery(PROJECT_ID, QUERY1);
  FetchedQuery fetchedQuery2 = new FetchedQuery(PROJECT_ID, QUERY2);
  QueryUsageStatistics stats1 = new QueryUsageStatistics(1, 10L, 100L);

  @Before
  public void setup() {
    when(fetcherService.fetchQueriesSinceLastDays(any(), anyInt()))
        .thenReturn(Arrays.asList(fetchedQuery1, fetchedQuery2));
    when(mockQueryRepository.saveAll(any())).thenReturn(null);
    when(mockFetcherJobRepository.save(any())).then(returnsFirstArg());
    fetchedQuery1.setStatistics(stats1);
    fetchedQuery2.setStatistics(stats1);
  }

  @Test
  public void fetchAllQueriesJobTest() throws InterruptedException {
    FetcherQueryJob job = new FetcherQueryJob(PROJECT_ID);
    service.fetchAllQueriesJob(job);
    TimeUnit.SECONDS.sleep(1);
    Assert.assertEquals(FetcherJobStatus.FINISHED, job.getStatus());
    Mockito.verify(mockFetcherJobRepository, Mockito.timeout(1000).times(2))
        .save(ArgumentMatchers.any(FetcherQueryJob.class));
    Mockito.verify(mockQueryRepository, Mockito.timeout(1000).times(1))
        .saveAll(ArgumentMatchers.any());
  }

  @Test
  public void createNewFetcherQueryJobTest() {
    FetcherQueryJob job1 = service.createNewFetcherQueryJob(PROJECT_ID, request1);
    Assert.assertEquals(PROJECT_ID, job1.getProjectId());
    Assert.assertEquals(7, job1.getTimeframe());

    FetcherQueryJob job2 = service.createNewFetcherQueryJob(PROJECT_ID, request2);
    Assert.assertEquals(PROJECT_ID, job2.getProjectId());
    Assert.assertEquals(14, job2.getTimeframe());
  }
}
