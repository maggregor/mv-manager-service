package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.OptimizationResult;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private Project project1 = mock(Project.class);
  private Project project2 = mock(Project.class);
  private Project project3 = mock(Project.class);
  private Project project4 = mock(Project.class);

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

    project1 = mock(Project.class);
    project2 = mock(Project.class);
    project3 = mock(Project.class);
    project4 = mock(Project.class);
    when(project1.getProjectId()).thenReturn("project1");
    when(project2.getProjectId()).thenReturn("project2");
    when(project3.getProjectId()).thenReturn("project3");
    when(project4.getProjectId()).thenReturn("project4");
    when(project1.getUsername()).thenReturn("email1");
    when(project2.getUsername()).thenReturn("email2");
    when(project3.getUsername()).thenReturn("email3");
    when(project4.getUsername()).thenReturn("");
    when(project1.isAutomatic()).thenReturn(true);
    when(project2.isAutomatic()).thenReturn(true);
    when(project3.isAutomatic()).thenReturn(false);
    when(project4.isAutomatic()).thenReturn(true);
  }

  @Test
  public void buildMaterializedViewsMessage() throws JsonProcessingException {
    List<OptimizationResult> mockResults;
    String jsonResults;
    final String expected =
        "[{\"datasetName\":\"dataset1\",\"statement\":\"query1\",\"mmvName\":\"achilio_12345\"},{\"datasetName\":\"dataset2\",\"statement\":\"query2\",\"mmvName\":\"achilio_67890\"},{\"datasetName\":\"dataset3\",\"statement\":\"query3\",\"mmvName\":\"achilio_13579\"}]";
    OptimizationResult mockResultNullStatement = mock(OptimizationResult.class);
    when(mockResultNullStatement.getStatement()).thenReturn(null);
    mockResults = Lists.newArrayList(result1, result2, result3);
    jsonResults = service.buildOptimizationMessage("secretKey", mockResults);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode;
    jsonNode = mapper.readTree(jsonResults).get("optimizationResults");
    assertEquals(3, jsonNode.size());
    assertEquals(expected, mapper.writeValueAsString(jsonNode));
    // mockResultNullStatement must be ignored
    mockResults = Lists.newArrayList(result1, result2, result3, mockResultNullStatement);
    jsonResults = service.buildOptimizationMessage("secretKey", mockResults);
    jsonNode = mapper.readTree(jsonResults).get("optimizationResults");
    assertEquals(3, jsonNode.size());
    assertEquals(expected, mapper.writeValueAsString(jsonNode));
  }

  @Test
  public void buildSchedulerMessage() throws JsonProcessingException {
    List<Project> mockProjects1;
    List<Project> mockProjects2;
    List<Project> mockProjects3;
    String jsonProjects1;
    String jsonProjects2;
    String jsonProjects3;
    final String expected1 =
        "[{\"projectId\":\"project1\",\"username\":\"email1\"},{\"projectId\":\"project2\",\"username\":\"email2\"}]";
    mockProjects1 = Lists.newArrayList(project1, project2);
    jsonProjects1 = service.buildSchedulerMessage(mockProjects1);
    assertEquals(expected1, jsonProjects1);

    final String expected2 =
        "[{\"projectId\":\"project1\",\"username\":\"email1\"},{\"projectId\":\"project2\",\"username\":\"email2\"}]";
    mockProjects2 = Lists.newArrayList(project1, project2, project3);
    jsonProjects2 = service.buildSchedulerMessage(mockProjects2);
    assertEquals(expected2, jsonProjects2);

    final String expected3 =
        "[{\"projectId\":\"project1\",\"username\":\"email1\"},{\"projectId\":\"project2\",\"username\":\"email2\"}]";
    mockProjects3 = Lists.newArrayList(project1, project2, project3, project4);
    jsonProjects3 = service.buildSchedulerMessage(mockProjects3);
    assertEquals(expected3, jsonProjects3);
  }
}
