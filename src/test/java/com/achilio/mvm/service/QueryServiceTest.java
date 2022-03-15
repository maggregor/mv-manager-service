package com.achilio.mvm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.services.QueryService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceTest {

  private final String PROJECT_ID1 = "myProjectId";
  private final String PROJECT_ID2 = "myOtherProjectId";
  private final String FETCHER_JOB_ID1 = "fetchedJobId1";
  private final String QUERY_ID1 = "queryId1";
  private final String QUERY_ID2 = "queryId2";
  private final LocalDate startTime = LocalDate.of(2020, 1, 8);
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private FetcherQueryJob JOB1 = new FetcherQueryJob(PROJECT_ID1);
  private Query QUERY1 =
      new Query(JOB1, "SELECT 1", QUERY_ID1, PROJECT_ID1, false, false, startTime, stats);
  private Query QUERY2 =
      new Query(JOB1, "SELECT 2", QUERY_ID2, PROJECT_ID1, false, false, startTime, stats);
  private FetcherQueryJob JOB2 = new FetcherQueryJob(PROJECT_ID1);
  @InjectMocks private QueryService service;
  @Mock private QueryRepository mockQueryRepository;
  @Mock private FetcherJobRepository mockFetcherJobRepository;

  @Before
  public void setup() {
    JOB1.setId(1L);
    JOB2.setId(2L);
    when(mockFetcherJobRepository.findFetcherQueryJobByIdAndProjectId(
            ArgumentMatchers.eq(1L), any()))
        .thenReturn(java.util.Optional.ofNullable(JOB1));
    when(mockFetcherJobRepository.findFetcherQueryJobByIdAndProjectId(
            ArgumentMatchers.eq(2L), any()))
        .thenReturn(java.util.Optional.ofNullable(JOB2));
    when(mockFetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(any()))
        .thenReturn(java.util.Optional.ofNullable(JOB1));
    when(mockQueryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            ArgumentMatchers.eq(JOB1), any()))
        .thenReturn(Arrays.asList(QUERY1, QUERY2));
    when(mockQueryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            ArgumentMatchers.eq(JOB2), any()))
        .thenReturn(Collections.emptyList());
    when(mockQueryRepository.findQueryByIdAndFetcherQueryJob_ProjectId(any(), any()))
        .thenReturn(java.util.Optional.ofNullable(QUERY1));
  }

  @Test
  public void getAllQueriesByJobIdAndProjectIdTest() {
    List<Query> queries = service.getAllQueriesByJobIdAndProjectId(JOB1.getId(), PROJECT_ID1);
    Assert.assertEquals(2, queries.size());
    Assert.assertEquals("SELECT 1", queries.get(0).getQuery());
    Assert.assertEquals("SELECT 2", queries.get(1).getQuery());

    queries = service.getAllQueriesByJobIdAndProjectId(JOB2.getId(), PROJECT_ID1);
    Assert.assertEquals(0, queries.size());
  }

  @Test
  public void getAllQueriesByProjectIdLastJobTest() {
    List<Query> queries = service.getAllQueriesByProjectIdLastJob(PROJECT_ID1);
    Assert.assertEquals(2, queries.size());
    Assert.assertEquals("SELECT 1", queries.get(0).getQuery());
    Assert.assertEquals("SELECT 2", queries.get(1).getQuery());
  }

  @Test
  public void getQueryTest() {
    Query query = service.getQuery(QUERY_ID1, PROJECT_ID1);
    Assert.assertEquals(QUERY_ID1, query.getId());
    Assert.assertEquals(PROJECT_ID1, query.getProjectId());
    Assert.assertEquals("SELECT 1", query.getQuery());
    try {
      service.getQuery("unknownId", PROJECT_ID1);
    } catch (QueryNotFoundException e) {
      Assert.assertEquals("Query unknownId not found", e.getMessage());
    }
  }
}