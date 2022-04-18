package com.achilio.mvm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
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
  private final String QUERY_ID2 = "queryId2";
  private final Date startTime = new Date();
  private final Query QUERY1 =
      new Query("SELECT 1", QUERY_ID1, PROJECT_ID1, "", false, false, startTime);
  private final Query QUERY2 =
      new Query("SELECT 2", QUERY_ID2, PROJECT_ID1, "", false, false, startTime);
  @InjectMocks private QueryService service;
  @Mock private QueryRepository mockQueryRepository;
  @Mock private FetcherJobRepository mockFetcherJobRepository;

  @Before
  public void setup() {
    when(mockQueryRepository.findQueryByIdAndProjectId(any(), any()))
        .thenReturn(java.util.Optional.of(QUERY1));
    when(mockQueryRepository.findQueryByIdAndProjectId(
            ArgumentMatchers.eq("unknownQueryId"), any()))
        .thenReturn(java.util.Optional.empty());
  }

  @Test
  public void getQuery() {
    Query query = service.getQuery(QUERY_ID1, PROJECT_ID1);
    Assert.assertEquals(QUERY_ID1, query.getId());
    Assert.assertEquals(PROJECT_ID1, query.getProjectId());
    Assert.assertEquals("SELECT 1", query.getQuery());

    Assert.assertThrows(
        QueryNotFoundException.class, () -> service.getQuery("unknownQueryId", PROJECT_ID1));
  }
}
