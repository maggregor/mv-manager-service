package com.achilio.mvm.service;

import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.QueryController;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.services.QueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
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
public class QueryControllerTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final int TIMEFRAME1 = 7;
  private final int TIMEFRAME2 = 14;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FetcherQueryJob realFetcherJob1 = new FetcherQueryJob(TEST_PROJECT_ID);
  private final FetcherQueryJob realFetcherJob2 = new FetcherQueryJob(TEST_PROJECT_ID, TIMEFRAME2);
  private final String queryStatement1 = "SELECT 1";
  private final String queryStatement2 = "SELECT 1";
  private final boolean useMaterializedView = false;
  private final boolean useCache = false;
  private final String googleJobId = "google-id";
  private final LocalDate startTime = LocalDate.of(2020, 1, 8);
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private Query query1;
  private Query query2;
  @InjectMocks private QueryController controller;
  @Mock private QueryService mockQueryService;

  @Before
  public void setup() {
    query1 =
        new Query(
            realFetcherJob1,
            queryStatement1,
            googleJobId,
            TEST_PROJECT_ID,
            useMaterializedView,
            useCache,
            startTime,
            stats);
    query2 =
        new Query(
            realFetcherJob1,
            queryStatement2,
            googleJobId,
            TEST_PROJECT_ID,
            useMaterializedView,
            useCache,
            startTime,
            stats);
    when(mockQueryService.getAllQueriesByJobIdAndProjectId(1L, TEST_PROJECT_ID))
        .thenReturn(Arrays.asList(query1, query2));
    when(mockQueryService.getAllQueries(TEST_PROJECT_ID)).thenReturn(Arrays.asList(query1, query2));
    when(mockQueryService.getQuery(googleJobId, TEST_PROJECT_ID)).thenReturn(query1);
    when(mockQueryService.getQuery("unknownQueryId", TEST_PROJECT_ID))
        .thenThrow(new QueryNotFoundException("unknownQueryId"));
  }

  @Test
  public void getAllQueriesByProjectIdAndFetcherQueryJobId() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<Query> jobResponseEntity =
        controller.getAllQueriesByProjectIdAndFetcherQueryJobId(TEST_PROJECT_ID, 1L);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(queryStatement1, map.get(0).get("query").asText());
  }

  @Test
  public void getAllQueriesByProjectId() {
    List<Query> jobResponseEntity = controller.getAllQueriesByProjectId(TEST_PROJECT_ID);
    Assert.assertEquals(2, jobResponseEntity.size());
    Assert.assertEquals(queryStatement1, jobResponseEntity.get(0).getQuery());
  }

  @Test
  public void getQuery() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Query jobResponseEntity = controller.getSingleQuery(TEST_PROJECT_ID, googleJobId);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    Assert.assertEquals(queryStatement1, map.get("query").asText());

    Assert.assertThrows(
        QueryNotFoundException.class,
        () -> {
          controller.getSingleQuery(TEST_PROJECT_ID, "unknownQueryId");
        });
  }
}
