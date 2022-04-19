package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.QueryRepository;
import com.achilio.mvm.service.services.QueryService;
import java.util.Date;
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
  private final String QUERY_ID1 = "queryId1";
  private final Date startTime = new Date();
  private final Query QUERY1 =
      new Query("SELECT 1", QUERY_ID1, PROJECT_ID1, "", false, false, startTime);
  @InjectMocks
  private QueryService service;
  @Mock
  private QueryRepository mockRepository;

  @Before
  public void setup() {
    when(mockRepository.findQueryByProjectIdAndId(any(), any()))
        .thenReturn(java.util.Optional.of(QUERY1));
    when(mockRepository.findQueryByProjectIdAndId(any(),
        ArgumentMatchers.eq("unknownQueryId")))
        .thenReturn(java.util.Optional.empty());
  }

  @Test
  public void getQuery() {
    Query query = service.getQuery(QUERY_ID1, PROJECT_ID1);
    assertEquals(QUERY_ID1, query.getId());
    assertEquals(PROJECT_ID1, query.getProjectId());
    assertEquals("SELECT 1", query.getQuery());

    Assert.assertThrows(
        QueryNotFoundException.class, () -> service.getQuery(PROJECT_ID1, "unknownQueryId"));
  }

  @Test
  public void when_getAverageProcessedBytesSince_thenCallRepository() {
    when(mockRepository.averageProcessedBytesByProjectAndStartTimeGreaterThanEqual(any(),
        any(Date.class))).thenReturn(100L);
    assertEquals(100L, service.getAverageProcessedBytesSince("", 0).longValue());
    verify(mockRepository,
        timeout(1000).times(1)).averageProcessedBytesByProjectAndStartTimeGreaterThanEqual(
        any(),
        any(Date.class));
  }


  @Test
  public void when_getCountQuerySince_thenCallRepository() {
    when(mockRepository.countQueryByProjectAndStartTimeGreaterThanEqual(any(),
        any(Date.class))).thenReturn(1234L);
    assertEquals(1234L, service.getTotalQuerySince("", 0).longValue());
    verify(mockRepository,
        timeout(1000).times(1)).countQueryByProjectAndStartTimeGreaterThanEqual(
        any(),
        any(Date.class));
  }

  @Test
  public void when_getPercentQuerySince_thenCallRepository() {
    when(mockRepository.countQueryByProjectAndStartTimeGreaterThanEqual(any(),
        any(Date.class))).thenReturn(1234L);
    when(mockRepository.countQueryInMVByProjectAndStartTimeGreaterThanEqual(any(),
        any(Date.class))).thenReturn(456L);
    assertEquals(37L, service.getPercentQueryInMVSince("", 0).longValue());
    verify(mockRepository,
        timeout(1000).times(1)).countQueryByProjectAndStartTimeGreaterThanEqual(
        any(),
        any(Date.class));
    verify(mockRepository,
        timeout(1000).times(1)).countQueryInMVByProjectAndStartTimeGreaterThanEqual(
        any(),
        any(Date.class));
  }

  @Test
  public void getPercentQuerySinceWithSpecialCases() {
    when(mockRepository.countQueryByProjectAndStartTimeGreaterThanEqual(any(),
        any())).thenReturn(1000L);
    when(mockRepository.countQueryInMVByProjectAndStartTimeGreaterThanEqual(any(),
        any())).thenReturn(0L);
    assertEquals(0L, service.getPercentQueryInMVSince("", 0).longValue());
    when(mockRepository.countQueryByProjectAndStartTimeGreaterThanEqual(any(),
        any())).thenReturn(1000L);
    when(mockRepository.countQueryInMVByProjectAndStartTimeGreaterThanEqual(any(),
        any())).thenReturn(1000L);
    assertEquals(100L, service.getPercentQueryInMVSince("", 0).longValue());
  }
}
