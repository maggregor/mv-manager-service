package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Ignore;
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
    when(result1.getStatement()).thenReturn("query 1");
    when(result2.getStatement()).thenReturn("query 2");
    when(result3.getStatement()).thenReturn("query 3");
  }

  @Test
  public void messageAsStatementSeparatedSemicolon() {
    List<OptimizationResult> mockResults = Lists.newArrayList(result1, result2, result3);
    final String EXPECTED = String.format("%s;%s;%s",
        result1.getStatement(),
        result2.getStatement(),
        result3.getStatement());
    assertEquals(EXPECTED, service.getFormattedMessage(mockResults));
  }

  @Test
  public void messageAsStatementJsonArray() throws JsonProcessingException {
    List<OptimizationResult> mockResults;
    String jsonResults;
    OptimizationResult mockResultNullStatement = mock(OptimizationResult.class);
    when(mockResultNullStatement.getStatement()).thenReturn(null);
    mockResults = Lists.newArrayList(result1, result2, result3);
    jsonResults = service.toJSONArrayOfResultStatements(mockResults);
    assertEquals("[\"query 1\",\"query 2\",\"query 3\"]", jsonResults);
    // mockResultNullStatement is ignored
    mockResults = Lists.newArrayList(result1, result2, result3, mockResultNullStatement);
    jsonResults = service.toJSONArrayOfResultStatements(mockResults);
    assertEquals("[\"query 1\",\"query 2\",\"query 3\"]", jsonResults);
  }

  @Test
  @Ignore
  public void testPublisher() {
    Optimization optimization = new Optimization("achilio-dev");
    GooglePublisherService publisherService = new GooglePublisherService();
    publisherService.publishOptimization(optimization, new ArrayList<>());
  }

}
