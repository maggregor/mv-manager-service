package com.achilio.mvm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.FetcherJobController;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherJob.FetcherJobStatus;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.exceptions.FetcherJobNotFoundException;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FetcherJobControllerTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final long TIMEFRAME1 = 7L;
  private final long TIMEFRAME2 = 14L;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FetcherQueryJob realFetcherJob1 = new FetcherQueryJob(TEST_PROJECT_ID);
  private final FetcherQueryJob realFetcherJob2 = new FetcherQueryJob(TEST_PROJECT_ID, TIMEFRAME2);

  @InjectMocks FetcherJobController controller;
  @Mock private FetcherJobRepository mockedFetcherJobRepository;

  @Before
  public void setup() {
    when(mockedFetcherJobRepository.save(any())).thenReturn(realFetcherJob1);
    when(mockedFetcherJobRepository.findFetcherQueryJobsByProjectId(anyString()))
        .thenReturn(Arrays.asList(realFetcherJob1, realFetcherJob2));
    when(mockedFetcherJobRepository.findTopFetchedQueryJobByProjectIdOrderByCreatedAtDesc(
            anyString()))
        .thenReturn(Optional.of(realFetcherJob1));
    when(mockedFetcherJobRepository.findFetcherQueryJobByProjectIdAndId(
            anyString(), ArgumentMatchers.eq(1L)))
        .thenReturn(Optional.of(realFetcherJob1));
    when(mockedFetcherJobRepository.findFetcherQueryJobByProjectIdAndId(
            anyString(), ArgumentMatchers.eq(2L)))
        .thenReturn(Optional.of(realFetcherJob2));
    when(mockedFetcherJobRepository.findFetcherQueryJobByProjectIdAndId(
            anyString(), ArgumentMatchers.eq(3L)))
        .thenReturn(Optional.empty());
    when(mockedFetcherJobRepository.findFetcherQueryJobsByProjectIdAndStatus(
            anyString(), ArgumentMatchers.eq(FetcherJobStatus.PENDING)))
        .thenReturn(Arrays.asList(realFetcherJob1, realFetcherJob2));
    when(mockedFetcherJobRepository.findTopFetchedQueryJobByProjectIdAndStatusOrderByCreatedAtDesc(
            anyString(), ArgumentMatchers.eq(FetcherJobStatus.PENDING)))
        .thenReturn(Optional.of(realFetcherJob2));
  }

  @Test
  public void getFetcherQueryJobByProjectIdNoParamTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<FetcherQueryJob> jobResponseEntity =
        controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, null, null);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get(1).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(1).get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(1).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(1).get("createdAt") instanceof NullNode);
  }

  @Test
  public void getFetcherQueryJobByProjectIdWithParamLastTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    List<FetcherQueryJob> jobResponseEntity =
        controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, true, null);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
  }

  @Test
  public void getFetcherQueryJobByProjectIdWithParamStatusTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    // List of pending
    List<FetcherQueryJob> jobResponseEntity =
        controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, false, "pending");
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(TEST_PROJECT_ID, map.get(0).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get(0).get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get(0).get("status").asText());
    Assert.assertTrue(map.get(0).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(0).get("createdAt") instanceof NullNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get(1).get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get(1).get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get(1).get("status").asText());
    Assert.assertTrue(map.get(1).get("id") instanceof NullNode);
    Assert.assertTrue(map.get(1).get("createdAt") instanceof NullNode);

    // No job with status
    jobResponseEntity =
        controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, false, "finished");
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(0, map.size());

    // Last job with status
    jobResponseEntity = controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, true, "pending");
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(1, map.size());

    // No Last job with status
    jobResponseEntity = controller.getFetcherQueryJobByProjectId(TEST_PROJECT_ID, true, "finished");
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ArrayNode);
    Assert.assertEquals(0, map.size());
  }

  @Test
  public void getFetcherQueryJobByProjectIdAndJobIdTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    FetcherJob jobResponseEntity = controller.getFetcherQueryJob(TEST_PROJECT_ID, 1L);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);

    jobResponseEntity = controller.getFetcherQueryJob(TEST_PROJECT_ID, 2L);
    jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    map = mapper.readTree(jsonResponse);
    Assert.assertTrue(map instanceof ObjectNode);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME2, map.get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);

    // Not found
    try {
      controller.getFetcherQueryJob(TEST_PROJECT_ID, 3L);
    } catch (FetcherJobNotFoundException e) {
      Assert.assertEquals("FetcherJob 3 not found", e.getMessage());
    }
  }

  @Test
  public void createNewFetcherQueryJobTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    FetcherJob jobResponseEntity = controller.createNewFetcherQueryJob(TEST_PROJECT_ID);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME1, map.get("timeframe").asLong());
    Assert.assertEquals(FetcherJobStatus.PENDING.toString(), map.get("status").asText());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);
  }
}
