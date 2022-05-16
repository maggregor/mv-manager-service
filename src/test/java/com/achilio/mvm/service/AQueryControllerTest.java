package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.QueryController;
import com.achilio.mvm.service.controllers.requests.QueryRequest;
import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.exceptions.QueryNotFoundException;
import com.achilio.mvm.service.services.QueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Date;
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
public class AQueryControllerTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String queryStatement1 = "SELECT 1";
  private final String queryStatement2 = "SELECT 1";
  private final boolean useMaterializedView = false;
  private final boolean useCache = false;
  private final String googleJobId = "google-id";
  private AQuery query1;
  private AQuery query2;
  @InjectMocks
  private QueryController controller;
  @Mock
  private QueryService mockService;

  @Before
  public void setup() {
    query1 =
        new AQuery(
            queryStatement1,
            googleJobId,
            TEST_PROJECT_ID,
            "",
            useMaterializedView,
            useCache,
            new Date());
    query2 =
        new AQuery(
            queryStatement2,
            googleJobId,
            TEST_PROJECT_ID,
            "",
            useMaterializedView,
            useCache,
            new Date());
    when(mockService.getAllQueries(TEST_PROJECT_ID)).thenReturn(Arrays.asList(query1, query2));
    when(mockService.getQuery(TEST_PROJECT_ID, googleJobId)).thenReturn(query1);
    when(mockService.getQuery(TEST_PROJECT_ID, "unknownQueryId"))
        .thenThrow(new QueryNotFoundException("unknownQueryId"));
  }

  @Test
  public void getAllQueriesByProjectId() {
    List<AQuery> jobResponseEntity = controller.getAllQueriesByProjectId(TEST_PROJECT_ID);
    assertEquals(2, jobResponseEntity.size());
    assertEquals(queryStatement1, jobResponseEntity.get(0).getQuery());
  }

  @Test
  public void getQuery() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    AQuery jobResponseEntity = controller.getSingleQuery(TEST_PROJECT_ID, googleJobId);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    assertEquals(queryStatement1, map.get("query").asText());

    Assert.assertThrows(
        QueryNotFoundException.class,
        () -> controller.getSingleQuery(TEST_PROJECT_ID, "unknownQueryId"));
  }

  @Test
  public void createQuery() {
    QueryRequest request = new QueryRequest(TEST_PROJECT_ID, "SELECT 1");
    AQuery query = controller.createQuery(request);
    assertEquals(TEST_PROJECT_ID, query.getProjectId());
    assertEquals("SELECT 1", query.getQuery());
  }

  @Test
  public void getStatistics() {
    when(mockService.getTotalQuerySince("project", 0)).thenReturn(100L);
    when(mockService.getAverageProcessedBytesSince("project", 0)).thenReturn(40L);
    when(mockService.getPercentQueryInMVSince("project", 0)).thenReturn(80L);
    Long total = controller.getStatistics("project", 0, "total_queries");
    Long avg = controller.getStatistics("project", 0, "average_processed_bytes");
    Long percent = controller.getStatistics("project", 0, "percent_query_in_mv");
    assertEquals(100L, total.longValue());
    assertEquals(40L, avg.longValue());
    assertEquals(80L, percent.longValue());
  }

  @Test
  public void when_getUnknownTypeStatistics_thenReturnException() {
    Exception e = assertThrows(IllegalArgumentException.class, () ->
        controller.getStatistics("", 0, "unknown_type")
    );
    assertEquals(IllegalArgumentException.class, e.getClass());
    assertEquals("Unknown statistics type", e.getMessage());
  }
}
