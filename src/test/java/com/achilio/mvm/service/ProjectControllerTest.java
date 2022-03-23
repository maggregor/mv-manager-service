package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ProjectController;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.AggregatedStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.DatasetResponse;
import com.achilio.mvm.service.controllers.responses.GlobalQueryStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.databases.entities.DefaultFetchedDataset;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.FetcherService.StatEntry;
import com.achilio.mvm.service.services.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Arrays;
import java.util.Collections;
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
public class ProjectControllerTest {

  private static final String TEST_PROJECT_ID1 = "achilio-dev";
  private static final String TEST_PROJECT_ID2 = "other-project";
  private static final String TEST_PROJECT_NAME1 = "Achilio Dev";
  private static final String TEST_PROJECT_NAME2 = "Other Project";
  private static final String TEST_DATASET_NAME1 = "myDataset1";
  private static final String TEST_DATASET_NAME2 = "myDataset2";
  private static final String STRIPE_SUBSCRIPTION_ID1 = "stripeSubscription1";
  private static final String STRIPE_SUBSCRIPTION_ID2 = "stripeSubscription2";
  private static final String STRIPE_SUBSCRIPTION_ID3 = "stripeSubscription3";
  private static final AOrganization ORGANIZATION =
      new AOrganization("organization/12345", "example.com", "stripeCustomerId", "workspaceId");
  private static final Project realProject = new Project("achilio-dev");
  private static final DefaultFetchedProject realFetchedProject =
      new DefaultFetchedProject(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
  private final ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks ProjectController controller;
  @Mock FetcherService mockedFetcherService;
  @Mock ProjectService mockedProjectService;

  @Before
  public void setup() throws JsonProcessingException {
    when(mockedProjectService.findProjectOrCreate(any())).thenReturn(realProject);
    when(mockedFetcherService.fetchProject(any())).thenReturn(realFetchedProject);
  }

  @Test
  public void contextLoads() {
    assertThat(controller).isNotNull();
  }

  @Test
  public void getProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1, ORGANIZATION);
    FetchedProject fetchedProject = new DefaultFetchedProject(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    when(mockedFetcherService.fetchProject(any())).thenReturn(fetchedProject);
    when(mockedProjectService.findProjectOrCreate(any())).thenReturn(project);
    ProjectResponse responseEntity;
    // Project 1
    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    assertProjectResponseEquals(project, responseEntity);

    // Project 2
    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    assertProjectResponseEquals(project, responseEntity);

    // Project 3
    project.setAutomaticAvailable(true);
    project.setActivated(true);
    project.setAutomatic(true);
    project.setMvMaxPerTable(10);
    project.setAnalysisTimeframe(14);
    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    assertProjectResponseEquals(project, responseEntity);
  }

  @Test
  public void whenOrgIsNull() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1, null);

    FetchedProject fetchedProject = new DefaultFetchedProject(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    when(mockedFetcherService.fetchProject(any())).thenReturn(fetchedProject);
    when(mockedProjectService.findProjectOrCreate(any())).thenReturn(project);
    ProjectResponse responseEntity;

    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    assertProjectResponseEquals(project, responseEntity);
  }

  @Test
  public void getAllProject() throws Exception {
    Project project1 = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1, ORGANIZATION);
    Project project2 = new Project(TEST_PROJECT_ID2, STRIPE_SUBSCRIPTION_ID2, ORGANIZATION);
    project1.setUsername("myEmail");
    project2.setAutomaticAvailable(true);
    project2.setActivated(true);
    project2.setAutomatic(true);
    project2.setMvMaxPerTable(10);
    project2.setAnalysisTimeframe(14);
    DefaultFetchedProject fetchedProject1 =
        new DefaultFetchedProject(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    DefaultFetchedProject fetchedProject2 =
        new DefaultFetchedProject(TEST_PROJECT_ID2, TEST_PROJECT_NAME2);
    when(mockedFetcherService.fetchAllProjects())
        .thenReturn(Arrays.asList(fetchedProject1, fetchedProject2));
    when(mockedProjectService.findProjectOrCreate(TEST_PROJECT_ID1)).thenReturn(project1);
    when(mockedProjectService.findProjectOrCreate(TEST_PROJECT_ID2)).thenReturn(project2);
    List<ProjectResponse> responseEntity = controller.getAllProjects();
    assertProjectResponseListEquals(Arrays.asList(project1, project2), responseEntity);
  }

  @Test
  public void updateProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1, ORGANIZATION);
    project.setAnalysisTimeframe(20);
    project.setMvMaxPerTable(10);
    project.setAutomaticAvailable(true);
    project.setAutomatic(true);
    UpdateProjectRequest payload = new UpdateProjectRequest(TEST_PROJECT_NAME1, true, 20, 10);
    when(mockedProjectService.updateProject(any(), any())).thenReturn(project);
    ProjectResponse responseEntity = controller.updateProject(TEST_PROJECT_ID1, payload);
    assertProjectResponseEquals(project, responseEntity);
  }

  @Test
  public void getAllDatasets() throws JsonProcessingException {
    DefaultFetchedDataset dataset1 =
        new DefaultFetchedDataset(
            TEST_PROJECT_ID1, TEST_DATASET_NAME1, null, null, null, null, null, null);
    DefaultFetchedDataset dataset2 =
        new DefaultFetchedDataset(
            TEST_PROJECT_ID1, TEST_DATASET_NAME2, null, null, null, null, null, null);
    when(mockedFetcherService.fetchAllDatasets(TEST_PROJECT_ID1))
        .thenReturn(Arrays.asList(dataset1, dataset2));
    when(mockedProjectService.isDatasetActivated(TEST_PROJECT_ID1, TEST_DATASET_NAME1))
        .thenReturn(true);
    when(mockedProjectService.isDatasetActivated(TEST_PROJECT_ID1, TEST_DATASET_NAME2))
        .thenReturn(false);
    List<DatasetResponse> responseEntity = controller.getAllDatasets(TEST_PROJECT_ID1);
    String expectedResponse =
        "[{\"projectId\":\"achilio-dev\",\"datasetName\":\"myDataset1\",\"activated\":true},{\"projectId\":\"achilio-dev\",\"datasetName\":\"myDataset2\",\"activated\":false}]";
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse, jsonResponse);
  }

  @Test
  public void getDataset() throws JsonProcessingException {
    DefaultFetchedDataset dataset =
        new DefaultFetchedDataset(
            TEST_PROJECT_ID1, TEST_DATASET_NAME1, null, null, null, null, null, null);
    when(mockedFetcherService.fetchDataset(TEST_PROJECT_ID1, TEST_DATASET_NAME1))
        .thenReturn(dataset);
    when(mockedProjectService.isDatasetActivated(TEST_PROJECT_ID1, TEST_DATASET_NAME1))
        .thenReturn(true);
    String expectedResponse =
        "{\"projectId\":\"achilio-dev\",\"datasetName\":\"myDataset1\",\"activated\":true}";
    DatasetResponse responseEntity = controller.getDataset(TEST_PROJECT_ID1, TEST_DATASET_NAME1);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse, jsonResponse);
  }

  @Test
  public void getQueryStatistics() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    GlobalQueryStatistics statistics1 = new GlobalQueryStatistics();
    when(mockedFetcherService.getStatistics(TEST_PROJECT_ID1, 30)).thenReturn(statistics1);
    GlobalQueryStatisticsResponse responseEntity1 =
        controller.getQueryStatistics(TEST_PROJECT_ID1, 30);
    String jsonResponse1 = objectMapper.writeValueAsString(responseEntity1);
    JsonNode map1 = mapper.readTree(jsonResponse1);
    assertEquals(0, map1.get("global").get("totalQueries").asInt());
    assertEquals(0, map1.get("global").get("totalBilledBytes").asInt());
    assertEquals(0, map1.get("global").get("totalProcessedBytes").asInt());

    GlobalQueryStatistics statistics2 = new GlobalQueryStatistics();
    when(mockedFetcherService.getStatistics(TEST_PROJECT_ID2, 30)).thenReturn(statistics2);
    GlobalQueryStatisticsResponse responseEntity2 =
        controller.getQueryStatistics(TEST_PROJECT_ID2, 30);
    String jsonResponse2 = objectMapper.writeValueAsString(responseEntity2);

    JsonNode map2 = mapper.readTree(jsonResponse2);
    assertEquals(0, map2.get("global").get("totalQueries").asInt());
    assertEquals(0, map2.get("global").get("totalBilledBytes").asInt());
    assertEquals(0, map2.get("global").get("totalProcessedBytes").asInt());
  }

  @Test
  public void getDailyStatistics() throws JsonProcessingException {
    StatEntry stat1 = new StatEntry(10, 1);
    StatEntry stat2 = new StatEntry(20, 2);
    when(mockedFetcherService.getDailyStatistics(TEST_PROJECT_ID1, 30))
        .thenReturn(Arrays.asList(stat1, stat2));
    String expectedResponse = "[{\"timestamp\":10,\"value\":1},{\"timestamp\":20,\"value\":2}]";
    List<StatEntry> responseEntity = controller.getDailyStatistics(TEST_PROJECT_ID1, 30);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse, jsonResponse);
  }

  @Test
  public void getKPIStatistics() throws Exception {
    GlobalQueryStatistics statistics = new GlobalQueryStatistics();
    when(mockedFetcherService.getStatistics(TEST_PROJECT_ID1, 30)).thenReturn(statistics);
    String expectedString =
        "{\"totalQueries\":0,\"percentQueriesIn\":0.0,\"averageScannedBytes\":0}";
    AggregatedStatisticsResponse responseEntity = controller.getKPIStatistics(TEST_PROJECT_ID1, 30);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedString, jsonResponse);
  }

  @Test
  public void getMissingPermissions() throws JsonProcessingException {
    /*
     * permissions in order are:
     * "bigquery.jobs.list",
     * "bigquery.datasets.get",
     * "resourcemanager.projects.get"
     */

    when(mockedFetcherService.fetchMissingPermissions(TEST_PROJECT_ID1))
        .thenReturn(Collections.emptyList());
    String expectedString = "[]";
    List<String> responseEntity = controller.getMissingPermissions(TEST_PROJECT_ID1);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedString, jsonResponse);

    when(mockedFetcherService.fetchMissingPermissions(TEST_PROJECT_ID1))
        .thenReturn(Collections.singletonList("bigquery.jobs.list"));
    expectedString = "[\"bigquery.jobs.list\"]";
    responseEntity = controller.getMissingPermissions(TEST_PROJECT_ID1);
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedString, jsonResponse);

    when(mockedFetcherService.fetchMissingPermissions(TEST_PROJECT_ID1))
        .thenReturn(Arrays.asList("bigquery.jobs.list", "bigquery.datasets.get"));
    expectedString = "[\"bigquery.jobs.list\",\"bigquery.datasets.get\"]";
    responseEntity = controller.getMissingPermissions(TEST_PROJECT_ID1);
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedString, jsonResponse);
  }

  private void assertProjectResponseEquals(Project expected, ProjectResponse actualProjectResponse)
      throws JsonProcessingException {
    String actualJson = objectMapper.writeValueAsString(actualProjectResponse);
    JsonNode jsonNode = objectMapper.readTree(actualJson);
    assertEquals(expected.getProjectId(), jsonNode.get("projectId").asText());
    assertEquals(expected.getUsername(), jsonNode.get("username").asText());
    assertEquals(
        expected.getMvMaxPerTable(), Integer.valueOf(jsonNode.get("mvMaxPerTable").asInt()));
    assertEquals(
        expected.getAnalysisTimeframe(),
        Integer.valueOf(jsonNode.get("analysisTimeframe").asInt()));
    assertEquals(expected.isActivated(), jsonNode.get("activated").asBoolean());
    assertEquals(expected.isAutomatic(), jsonNode.get("automatic").asBoolean());
    assertEquals(expected.getStripeSubscriptionId(), jsonNode.get("stripeSubscriptionId").asText());
    assertOrganizationResponseNodeEquals(expected.getOrganization(), jsonNode.get("organization"));
  }

  private void assertOrganizationResponseNodeEquals(AOrganization expected, JsonNode actual) {
    if (expected == null) {
      assertEquals(JsonNodeType.NULL, actual.getNodeType());
    } else {
      assertEquals(expected.getId(), actual.get("id").asText());
      assertEquals(expected.getName(), actual.get("name").asText());
      assertEquals(expected.getStripeCustomerId(), actual.get("stripeCustomerId").asText());
      assertEquals(expected.getGoogleWorkspaceId(), actual.get("googleWorkspaceId").asText());
    }
  }

  private void assertProjectResponseListEquals(
      List<Project> expected, List<ProjectResponse> actualProjectResponseList)
      throws JsonProcessingException {
    Assert.assertEquals(expected.size(), actualProjectResponseList.size());
    String actualJson = objectMapper.writeValueAsString(actualProjectResponseList);
    JsonNode jsonNode = objectMapper.readTree(actualJson);
    Assert.assertTrue(jsonNode instanceof ArrayNode);
    for (int i = 0; i < expected.size(); i++) {
      assertProjectResponseEquals(expected.get(i), actualProjectResponseList.get(i));
    }
  }
}
