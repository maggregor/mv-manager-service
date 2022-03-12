package com.achilio.mvm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.FetcherJobController;
import com.achilio.mvm.service.entities.FetcherJob;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
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
public class FetcherJobControllerTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final long TIMEFRAME = 7L;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FetcherQueryJob realFetcherJob = new FetcherQueryJob(TEST_PROJECT_ID);

  @InjectMocks FetcherJobController controller;
  @Mock private FetcherJobRepository mockedFetcherJobRepository;

  @Before
  public void setup() {
    when(mockedFetcherJobRepository.save(any())).thenReturn(realFetcherJob);
  }

  @Test
  public void createNewFetcherQueryJobTest() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    FetcherJob jobResponseEntity = controller.createNewFetcherQueryJob(TEST_PROJECT_ID);
    String jsonResponse = objectMapper.writeValueAsString(jobResponseEntity);
    JsonNode map = mapper.readTree(jsonResponse);
    Assert.assertEquals(TEST_PROJECT_ID, map.get("projectId").asText());
    Assert.assertEquals(TIMEFRAME, map.get("timeframe").asLong());
    Assert.assertTrue(map.get("id") instanceof NullNode);
    Assert.assertTrue(map.get("createdAt") instanceof NullNode);
  }
}
