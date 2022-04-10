package com.achilio.mvm.service;

import static com.achilio.mvm.service.entities.Job.JobStatus.FINISHED;
import static com.achilio.mvm.service.entities.Job.JobStatus.PENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.FetcherJobController;
import com.achilio.mvm.service.controllers.requests.FetcherQueryJobRequest;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Job;
import com.achilio.mvm.service.entities.Job.JobStatus;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.services.FetcherJobService;
import com.achilio.mvm.service.services.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FetcherJobControllerTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final int TIMEFRAME1 = 7;
  private final int TIMEFRAME2 = 14;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FetcherQueryJob realFetcherJob1 = new FetcherQueryJob(TEST_PROJECT_ID);
  private final FetcherQueryJob realFetcherJob2 = new FetcherQueryJob(TEST_PROJECT_ID, TIMEFRAME2);
  private final FetcherQueryJob realFetcherJob3 = new FetcherQueryJob(TEST_PROJECT_ID, TIMEFRAME2);
  private final FetcherQueryJobRequest request1 = new FetcherQueryJobRequest(TEST_PROJECT_ID, null);
  private final FetcherQueryJobRequest request2 =
      new FetcherQueryJobRequest(TEST_PROJECT_ID, TIMEFRAME2);

  @InjectMocks FetcherJobController controller;
  @Mock private FetcherJobService mockedFetcherJobService;
  @Mock private ProjectService mockedProjectService;

  @Before
  public void setup() {
    MockHelper.setupMockedAuthenticationContext();
    realFetcherJob3.setStatus(JobStatus.FINISHED);
    doNothing().when(mockedFetcherJobService).fetchAllQueriesJob(any(), any());
    when(mockedFetcherJobService.getAllQueryJobs(TEST_PROJECT_ID, null))
        .thenReturn(Arrays.asList(realFetcherJob1, realFetcherJob2, realFetcherJob3));
    when(mockedFetcherJobService.getLastFetcherQueryJob(TEST_PROJECT_ID, null))
        .thenReturn(Optional.of(realFetcherJob3));
    when(mockedFetcherJobService.getLastFetcherQueryJob(TEST_PROJECT_ID, PENDING))
        .thenReturn(Optional.of(realFetcherJob2));
    when(mockedFetcherJobService.getLastFetcherQueryJob(TEST_PROJECT_ID, FINISHED))
        .thenReturn(Optional.of(realFetcherJob3));
    when(mockedFetcherJobService.getAllQueryJobs(TEST_PROJECT_ID, PENDING))
        .thenReturn(Arrays.asList(realFetcherJob1, realFetcherJob2));
    when(mockedFetcherJobService.getAllQueryJobs(TEST_PROJECT_ID, FINISHED))
        .thenReturn(Collections.singletonList(realFetcherJob3));
    when(mockedFetcherJobService.getFetcherQueryJob(1L, TEST_PROJECT_ID))
        .thenReturn(Optional.of(realFetcherJob1));
    when(mockedFetcherJobService.getFetcherQueryJob(2L, TEST_PROJECT_ID))
        .thenReturn(Optional.of(realFetcherJob2));
    when(mockedFetcherJobService.createNewFetcherQueryJob(TEST_PROJECT_ID, request1))
        .thenReturn(realFetcherJob1);
    when(mockedFetcherJobService.createNewFetcherQueryJob(TEST_PROJECT_ID, request2))
        .thenReturn(realFetcherJob2);
    when(mockedProjectService.getProject(any(), any())).thenReturn(mock(Project.class));
  }

  @Test
  public void getFetcherQueryJobByProjectIdNoParam() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<FetcherQueryJob> jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, null, null);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(3, map.size());
    // Job1
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
    // Job2
    Assert.assertEquals(TEST_PROJECT_ID, map.get(1).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(1).get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(1).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(1).get("createdAt") instanceof NullNode);
    // Job3
    Assert.assertEquals(TEST_PROJECT_ID, map.get(2).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(2).get("timeframe").asLong());
    Assert.assertEquals(JobStatus.FINISHED.toString(), map.get(2).get("status").asText());
    Assert.assertTrue(map.get(2).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(2).get("createdAt") instanceof NullNode);
  }

  @Test
  public void getFetcherQueryJobByProjectIdWithParamLast() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<FetcherQueryJob> jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, true, null);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(JobStatus.FINISHED.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
  }

  @Test
  public void getFetcherQueryJobByProjectIdWithParamStatus() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    // List of pending
    List<FetcherQueryJob> jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, false, PENDING);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get(1).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(1).get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get(1).get("status").asText());
    Assert.assertTrue(map.get(1).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(1).get("createdAt") instanceof NullNode);

    // No job with status
    jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, false, FINISHED);
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());

    // Last job with status
    jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, true, PENDING);
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());

    // No Last job with status
    jobResponseEntity =
        controller.getAllFetcherQueryJobsByProjectId(TEST_PROJECT_ID, true, FINISHED);
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());
  }

  @Test
  public void getFetcherQueryJobByProjectIdAndJobId() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Job jobResponseEntity = controller.getFetcherQueryJob(TEST_PROJECT_ID, 1L);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);

    jobResponseEntity = controller.getFetcherQueryJob(TEST_PROJECT_ID, 2L);
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);

    // Not found
    Assert.assertThrows(
        FetcherJobNotFoundException.class,
        () -> controller.getFetcherQueryJob(TEST_PROJECT_ID, -1L));
  }

  @Test
  public void createNewFetcherQueryJobRequest1() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    // Request 1
    Job jobResponseEntity = controller.createNewFetcherQueryJob(request1);
    Mockito.verify(mockedFetcherJobService, Mockito.timeout(1000).times(1))
        .fetchAllQueriesJob(ArgumentMatchers.any(FetcherQueryJob.class), any());
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);
  }

  @Test
  public void createNewFetcherQueryJobRequest2() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    // Request 2
    Job jobResponseEntity = controller.createNewFetcherQueryJob(request2);
    Mockito.verify(mockedFetcherJobService, Mockito.timeout(1000).times(1))
        .fetchAllQueriesJob(ArgumentMatchers.any(FetcherQueryJob.class), any());
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get("timeframe").asLong());
    Assert.assertEquals(PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);
  }
}
