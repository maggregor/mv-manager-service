package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ProjectController;
import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.AggregatedStatisticsResponse;
import com.achilio.mvm.service.controllers.responses.DatasetResponse;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.databases.entities.DefaultFetchedDataset;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.statistics.GlobalQueryStatistics;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.services.StripeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
  private static final String TEAM_NAME1 = "myTeam1";
  private static final String TEAM_ID2 = "myTeam2";
  private static final Long CONNECTION_ID1 = 1L;
  private static final Project project1 = new Project(TEST_PROJECT_ID1);
  private static final ADataset dataset1 = new ADataset(project1, TEST_DATASET_NAME1);
  private static final ADataset dataset2 = new ADataset(project1, TEST_DATASET_NAME2);
  private final ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks ProjectController controller;
  @Mock ProjectService mockedProjectService;
  @Mock StripeService mockedStripeService;
  @Mock private UserProfile mockedUserProfile;

  @Before
  public void setup() throws JsonProcessingException {
    Authentication mockedAuth = mock(Authentication.class);
    SecurityContext mockedSecurityContext = mock(SecurityContext.class);
    when(mockedProjectService.getAllActivatedProjects(any())).thenReturn(Collections.emptyList());
    doNothing().when(mockedStripeService).updateSubscriptionQuantity(any(), any());
    when(mockedUserProfile.getTeamName()).thenReturn(TEAM_NAME1);
    when(mockedAuth.getDetails()).thenReturn(mockedUserProfile);
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
  }

  @Test
  public void contextLoads() {
    assertThat(controller).isNotNull();
  }

  @Test
  public void getAllProject() throws Exception {
    Project project1 = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1);
    Project project2 = new Project(TEST_PROJECT_ID2, STRIPE_SUBSCRIPTION_ID2);
    project1.setTeamName(TEAM_NAME1);
    project2.setAutomaticAvailable(true);
    project2.setActivated(true);
    project2.setAutomatic(true);
    project2.setMvMaxPerTable(10);
    project2.setAnalysisTimeframe(14);
    when(mockedProjectService.getAllActivatedProjects(TEAM_NAME1))
        .thenReturn(Arrays.asList(project1, project2));
    List<ProjectResponse> responseEntity = controller.getAllProjects();
    assertProjectResponseListEquals(Arrays.asList(project1, project2), responseEntity);
  }

  @Test
  public void getAllProject__empty() throws Exception {
    Project localProject1 = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1);
    Project localProject2 = new Project(TEST_PROJECT_ID2, STRIPE_SUBSCRIPTION_ID2);
    localProject1.setTeamName(TEAM_NAME1);
    localProject2.setTeamName(TEAM_ID2);
    localProject2.setAutomaticAvailable(true);
    localProject2.setActivated(true);
    localProject2.setAutomatic(true);
    localProject2.setMvMaxPerTable(10);
    localProject2.setAnalysisTimeframe(14);
    List<ProjectResponse> responseEntity = controller.getAllProjects();
    assertProjectResponseListEquals(Collections.emptyList(), responseEntity);
  }

  @Test
  public void getProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1);
    when(mockedProjectService.getProject(TEST_PROJECT_ID1, TEAM_NAME1)).thenReturn(project);
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
  public void getProject__whenProjectNotExist_throwException() {
    when(mockedProjectService.getProject(TEST_PROJECT_ID2, TEAM_NAME1))
        .thenThrow(new ProjectNotFoundException(TEST_PROJECT_ID2));
    Assert.assertThrows(
        ProjectNotFoundException.class, () -> controller.getProject(TEST_PROJECT_ID2));
  }

  @Test
  public void createProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, TEST_PROJECT_NAME1, STRIPE_SUBSCRIPTION_ID1);
    ACreateProjectRequest payload = new ACreateProjectRequest(TEST_PROJECT_ID1, CONNECTION_ID1);
    when(mockedProjectService.createProject(payload, TEAM_NAME1)).thenReturn(project);
    assertProjectResponseEquals(project, controller.createProject(payload));
  }

  @Test
  public void deleteProject() {
    controller.deleteProject(TEST_PROJECT_ID1);
    Mockito.verify(mockedProjectService, Mockito.timeout(1000).times(1))
        .deleteProject(TEST_PROJECT_ID1, TEAM_NAME1);
  }

  @Test
  public void deleteProject__whenProjectNotExists_throwException() {
    doThrow(new ProjectNotFoundException(TEST_PROJECT_ID1))
        .when(mockedProjectService)
        .deleteProject(TEST_PROJECT_ID1, TEAM_NAME1);
    assertThrows(ProjectNotFoundException.class, () -> controller.deleteProject(TEST_PROJECT_ID1));
  }

  // Old Controller methods

  @Test
  public void updateProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, STRIPE_SUBSCRIPTION_ID1);
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
    when(mockedProjectService.getAllDatasets(TEST_PROJECT_ID1, TEAM_NAME1))
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
    when(mockedProjectService.getFetchedDataset(TEST_PROJECT_ID1, TEAM_NAME1, TEST_DATASET_NAME1))
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
  public void getKPIStatistics() throws Exception {
    GlobalQueryStatistics statistics = new GlobalQueryStatistics();
    when(mockedProjectService.getStatistics(TEST_PROJECT_ID1, TEAM_NAME1, 30))
        .thenReturn(statistics);
    String expectedString =
        "{\"totalQueries\":0,\"percentQueriesIn\":0.0,\"averageScannedBytes\":0}";
    AggregatedStatisticsResponse responseEntity = controller.getKPIStatistics(TEST_PROJECT_ID1, 30);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
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
