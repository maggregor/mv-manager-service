package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GooglePublisherServiceTest {

  private final GooglePublisherService service = new GooglePublisherService();
  private OptimizationResult result1 = mock(OptimizationResult.class);
  private OptimizationResult result2 = mock(OptimizationResult.class);
  private OptimizationResult result3 = mock(OptimizationResult.class);

  @Before
  public void setUp() {
    result1 = mock(OptimizationResult.class);
    result2 = mock(OptimizationResult.class);
    result3 = mock(OptimizationResult.class);
    when(result1.getStatement()).thenReturn("query1");
    when(result2.getStatement()).thenReturn("query2");
    when(result3.getStatement()).thenReturn("query3");
    when(result1.getDatasetName()).thenReturn("dataset1");
    when(result2.getDatasetName()).thenReturn("dataset2");
    when(result3.getDatasetName()).thenReturn("dataset3");
    when(result1.getMvName()).thenReturn("achilio_12345");
    when(result2.getMvName()).thenReturn("achilio_67890");
    when(result3.getMvName()).thenReturn("achilio_13579");
  }

  @Test
  public void buildPublishMessage() throws JsonProcessingException {
    List<OptimizationResult> mockResults;
    String jsonResults;
    final String expected =
        "[{\"datasetName\":\"dataset1\",\"statement\":\"query1\",\"mmvName\":\"achilio_12345\"},{\"datasetName\":\"dataset2\",\"statement\":\"query2\",\"mmvName\":\"achilio_67890\"},{\"datasetName\":\"dataset3\",\"statement\":\"query3\",\"mmvName\":\"achilio_13579\"}]";
    OptimizationResult mockResultNullStatement = mock(OptimizationResult.class);
    when(mockResultNullStatement.getStatement()).thenReturn(null);
    mockResults = Lists.newArrayList(result1, result2, result3);
    jsonResults = service.buildMaterializedViewsMessage(mockResults);
    assertEquals(expected, jsonResults);
    // mockResultNullStatement must be ignored
    mockResults = Lists.newArrayList(result1, result2, result3, mockResultNullStatement);
    jsonResults = service.buildMaterializedViewsMessage(mockResults);
    assertEquals(expected, jsonResults);
  }
}
