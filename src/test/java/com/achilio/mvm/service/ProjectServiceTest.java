package com.achilio.mvm.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.ACreateProjectRequest;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.models.UserProfile;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.achilio.mvm.service.services.ConnectionService;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.achilio.mvm.service.services.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.model.Product;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

  private static final String TEAM_NAME1 = "myTeamName";
  private static final String TEAM_NOT_EXISTS = "notExistsTeamName";
  private static final Long CONNECTION_ID = 1L;
  private static final String CONNECTION_CONTENT = "serviceAccountContent";
  private static final UserProfile USER_PROFILE_1 =
      new UserProfile("moi", "moi@achilio.com", "foo", "bar", "myName", TEAM_NAME1);
  private static final String TEST_PROJECT_ID1 = "achilio-dev";
  private static final String TEST_PROJECT_ID2 = "other-project";
  private static final String TEST_PROJECT_ID3 = "new-project";
  private static final String PROJECT_NOT_EXISTS = "notExists";
  private static final String TEST_PROJECT_NAME1 = "Achilio Dev";
  private static final String TEST_PROJECT_NAME2 = "Other Project";
  private static final String TEST_PROJECT_NAME3 = "New Project";
  private static final FetchedProject mockedFetchedProject1 = mock(FetchedProject.class);
  private static final FetchedProject mockedFetchedProject2 = mock(FetchedProject.class);
  private static final FetchedProject mockedFetchedProject3 = mock(FetchedProject.class);
  private static final String TEST_DATASET_NAME1 = "nyc_trips";
  private static final String TEST_DATASET_NAME2 = "another_one";
  private static final String TEST_DATASET_NAME3 = "other_dataset";
  private static final String ORGANIZATION_NAME = "achilio.com";
  private static final String STRIPE_SUBSCRIPTION_ID = "sub_123456";
  private static final Project project1 =
      new Project(TEST_PROJECT_ID1, TEST_PROJECT_NAME1, STRIPE_SUBSCRIPTION_ID);
  private static final Project project2 =
      new Project(TEST_PROJECT_ID2, TEST_PROJECT_NAME2, STRIPE_SUBSCRIPTION_ID);
  private static final List<Project> activatedProjects = Arrays.asList(project1, project2);
  private static final Project project3 =
      new Project(TEST_PROJECT_ID3, TEST_PROJECT_NAME3, STRIPE_SUBSCRIPTION_ID);
  private static final Connection mockedConnection = mock(Connection.class);
  private static final ADataset mockedDataset1 = mock(ADataset.class);
  private static final ADataset mockedDataset2 = mock(ADataset.class);
  private final ADataset realDataset = new ADataset(project1, TEST_DATASET_NAME3);
  private final Map<String, String> productMetadata = new HashMap<>();
  private final Map<String, String> errorProductMetadata = new HashMap<>();
  @InjectMocks private ProjectService service;
  @Mock private ProjectRepository mockedProjectRepository;
  @Mock private DatasetRepository mockedDatasetRepository;
  @Mock private Product mockedProduct;
  @Mock private Product errorMockedProduct;
  @Mock private FetcherService mockedFetcherService;
  @Mock private Authentication mockedJWTAuth;
  @Mock private SecurityContext mockedSecurityContext;
  @Mock private ConnectionService mockedConnectionService;
  @Mock private GooglePublisherService mockedPublisherService;

  @Before
  public void setup() throws JsonProcessingException {
    when(mockedSecurityContext.getAuthentication()).thenReturn(mockedJWTAuth);
    SecurityContextHolder.setContext(mockedSecurityContext);
    when(mockedJWTAuth.getDetails()).thenReturn(USER_PROFILE_1);
    when(mockedProjectRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(project1));
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID2))
        .thenReturn(Optional.of(project2));
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID3)).thenReturn(Optional.empty());
    when(mockedProjectRepository.findAllByActivated(true)).thenReturn(activatedProjects);
    when(mockedProjectRepository.findAllByActivatedAndTeamName(true, TEAM_NAME1))
        .thenReturn(activatedProjects);
    when(mockedProjectRepository.findByProjectIdAndTeamName(TEST_PROJECT_ID1, TEAM_NAME1))
        .thenReturn(Optional.of(project1));
    when(mockedDataset1.isActivated()).thenReturn(true);
    when(mockedDataset2.isActivated()).thenReturn(false);
    when(mockedDatasetRepository.save(any(ADataset.class))).thenReturn(mockedDataset1);
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            project1.getProjectId(), TEST_DATASET_NAME1))
        .thenReturn(Optional.of(mockedDataset1));
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            project1.getProjectId(), TEST_DATASET_NAME2))
        .thenReturn(Optional.of(mockedDataset2));
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            project1.getProjectId(), TEST_DATASET_NAME3))
        .thenReturn(Optional.of(realDataset));
    productMetadata.put("mv_max", "10");
    productMetadata.put("automatic_available", "true");
    when(mockedProduct.getMetadata()).thenReturn(productMetadata);
    when(errorMockedProduct.getMetadata()).thenReturn(errorProductMetadata);
    when(mockedFetchedProject1.getProjectId()).thenReturn(TEST_PROJECT_ID1);
    when(mockedFetchedProject1.getName()).thenReturn(TEST_PROJECT_NAME1);
    when(mockedFetchedProject2.getProjectId()).thenReturn(TEST_PROJECT_ID2);
    when(mockedFetchedProject2.getName()).thenReturn(TEST_PROJECT_NAME2);
    when(mockedFetchedProject3.getProjectId()).thenReturn(TEST_PROJECT_ID3);
    when(mockedFetchedProject3.getName()).thenReturn(TEST_PROJECT_NAME3);
    when(mockedConnection.getContent()).thenReturn(CONNECTION_CONTENT);
    when(mockedConnectionService.getConnection(any(), any())).thenReturn(mockedConnection);
  }

  @Test
  public void getAllProjects() {
    List<Project> allProjects = service.getAllActivatedProjects(TEAM_NAME1);
    assertProjectListEquals(activatedProjects, allProjects);
  }

  @Test
  public void getAllProjects__whenEmpty() {
    when(mockedProjectRepository.findAllByActivatedAndTeamName(true, TEAM_NAME1))
        .thenReturn(Collections.emptyList());
    List<Project> allProjects = service.getAllActivatedProjects(TEAM_NAME1);
    assertProjectListEquals(Collections.emptyList(), allProjects);
  }

  @Test
  public void getProject() {
    when(mockedProjectRepository.findByProjectIdAndTeamName(TEST_PROJECT_ID1, TEAM_NAME1))
        .thenReturn(Optional.of(project1));
    Project project = service.getProject(TEST_PROJECT_ID1, TEAM_NAME1);
    assertProjectEquals(project1, project);
  }

  @Test
  public void getProject__whenNotExists_throwException() {
    when(mockedProjectRepository.findByProjectIdAndTeamName(TEST_PROJECT_ID1, TEAM_NAME1))
        .thenReturn(Optional.empty());
    Assert.assertThrows(
        ProjectNotFoundException.class, () -> service.getProject(TEST_PROJECT_ID1, TEAM_NAME1));
  }

  @Test
  public void createProject() {
    ACreateProjectRequest payload = new ACreateProjectRequest(TEST_PROJECT_ID3, CONNECTION_ID);
    when(mockedFetcherService.fetchProject(TEST_PROJECT_ID3, mockedConnection))
        .thenReturn(mockedFetchedProject3);
    Project project = service.createProject(payload, TEAM_NAME1);
    assertProjectEquals(project3, project);
    assertEquals(TEAM_NAME1, project.getTeamName());
    assertTrue(project.isActivated());
  }

  @Test
  public void createProject__whenExists_SetActivated() {
    ACreateProjectRequest payload = new ACreateProjectRequest(TEST_PROJECT_ID1, CONNECTION_ID);
    when(mockedFetcherService.fetchProject(TEST_PROJECT_ID1, mockedConnection))
        .thenReturn(mockedFetchedProject1);
    Project project = service.createProject(payload, TEAM_NAME1);
    Assert.assertTrue(project.isActivated());
  }

  @Test
  public void deleteProject() {
    project1.setTeamName(TEAM_NAME1);
    project1.setActivated(true);
    when(mockedProjectRepository.findByProjectIdAndTeamName(TEST_PROJECT_ID1, TEAM_NAME1))
        .thenReturn(Optional.of(project1));
    service.deleteProject(TEST_PROJECT_ID1, TEAM_NAME1);
    Mockito.verify(mockedProjectRepository, Mockito.timeout(1000).times(1))
        .save(any(Project.class));
    assertFalse(project1.isActivated());
  }

  @Test
  public void deleteProject__whenNotFound_throwException() {
    when(mockedProjectRepository.findByProjectIdAndTeamName(TEST_PROJECT_ID1, TEAM_NAME1))
        .thenReturn(Optional.empty());
    service.deleteProject(TEST_PROJECT_ID1, TEAM_NAME1);
    Mockito.verify(mockedProjectRepository, Mockito.timeout(1000).times(0))
        .save(any(Project.class));
  }

  // Old ProjectServiceTest methods

  //  @Test
  //  public void createProjectFromFetched() {
  //    Project project = service.createProject(TEST_PROJECT_ID1);
  //    assertEquals(project1.getProjectId(), project.getProjectId());
  //  }

  @Test
  public void findProject() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(project1));
    Assert.assertTrue(service.findProject(TEST_PROJECT_ID1).isPresent());
    assertFalse(service.findProject("unknown_project_id").isPresent());
  }

  @Test
  public void projectExists() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(project1));
    assertTrue(service.projectExists(TEST_PROJECT_ID1));
    assertFalse(service.projectExists("unknown_project_id"));
  }

  @Test
  public void activateProject() {
    Project project = new Project(TEST_PROJECT_ID1);
    assertTrue(project.isActivated());
    project.setActivated(false);
    assertFalse(project.isActivated());
    service.activateProject(project);
    assertTrue(project.isActivated());
  }

  @Test
  public void updateProject() {
    Integer analysisTimeFrame = 14;
    Integer mvMaxPerTable = 12;
    UpdateProjectRequest payload1 =
        new UpdateProjectRequest(TEST_PROJECT_NAME1, false, analysisTimeFrame, mvMaxPerTable);
    Project project = service.updateProject(TEST_PROJECT_ID2, payload1);
    assertEquals(TEST_PROJECT_ID2, project.getProjectId());
    assertFalse(project.isAutomatic());
    assertEquals(14, project.getAnalysisTimeframe());
    assertEquals(12, project.getMvMaxPerTable());
    project.setAutomaticAvailable(true);
    analysisTimeFrame = null;
    mvMaxPerTable = null;
    UpdateProjectRequest payload2 =
        new UpdateProjectRequest(null, true, analysisTimeFrame, mvMaxPerTable);
    project = service.updateProject(TEST_PROJECT_ID2, payload2);
    assertTrue(project.isAutomatic());
    assertEquals("moi@achilio.com", project.getUsername());
  }

  @Test
  public void deactivateProject() {
    Project project = new Project(TEST_PROJECT_ID1);
    project.setAutomaticAvailable(true);
    project.setActivated(true);
    project.setAutomatic(true);
    assertTrue(project.isActivated());
    assertTrue(project.isAutomatic());
    service.deactivateProject(project);
    assertFalse(project.isActivated());
    assertFalse(project.isAutomatic());
    assertFalse(project.isAutomaticAvailable());
    assertEquals(0, project.getMvMaxPerTableLimit());
    assertEquals(0, project.getMvMaxPerTable());
  }

  @Test
  public void updateMvMaxPerTableLimit() {
    Project project = new Project(TEST_PROJECT_ID1);
    assertEquals(5, project.getMvMaxPerTable());
    assertEquals(20, project.getMvMaxPerTableLimit());
    service.updateMvMaxPerTableLimit(project, 3);
    assertEquals(3, project.getMvMaxPerTable());
    assertEquals(3, project.getMvMaxPerTableLimit());
    service.updateMvMaxPerTableLimit(project, 12);
    assertEquals(3, project.getMvMaxPerTable());
    assertEquals(12, project.getMvMaxPerTableLimit());
  }

  @Test
  public void updateProjectAutomaticAvailable() {
    Project project = new Project(TEST_PROJECT_ID1);
    assertFalse(project.isAutomatic());
    assertFalse(project.isAutomaticAvailable());
    service.updateProjectAutomaticAvailable(project, true);
    assertFalse(project.isAutomatic());
    assertTrue(project.isAutomaticAvailable());
    project.setAutomatic(true);
    assertTrue(project.isAutomatic());
    service.updateProjectAutomaticAvailable(project, false);
    assertFalse(project.isAutomatic());
    assertFalse(project.isAutomaticAvailable());
  }

  @Test
  public void updatePlanSettings() {
    Project project = new Project(TEST_PROJECT_ID1);
    assertFalse(project.isAutomaticAvailable());
    assertEquals(20, project.getMvMaxPerTableLimit());
    service.updatePlanSettings(project, mockedProduct);
    assertTrue(project.isAutomaticAvailable());
    assertEquals(10, project.getMvMaxPerTableLimit());
    service.updatePlanSettings(project, errorMockedProduct);
    assertTrue(project.isAutomaticAvailable());
    assertEquals(10, project.getMvMaxPerTableLimit());
  }

  @Test
  public void updateDataset() {
    ADataset dataset = service.updateDataset(TEST_PROJECT_ID1, TEST_DATASET_NAME3, true);
    assertTrue(dataset.isActivated());
    assertEquals(TEST_DATASET_NAME3, "other_dataset");
    dataset = service.updateDataset(TEST_PROJECT_ID1, TEST_DATASET_NAME3, false);
    assertFalse(dataset.isActivated());
  }

  @Test
  public void isDatasetActivated() {
    assertTrue(service.isDatasetActivated(TEST_PROJECT_ID1, TEST_DATASET_NAME1));
    assertFalse(service.isDatasetActivated(TEST_PROJECT_ID1, TEST_DATASET_NAME2));
  }

  private void assertProjectListEquals(List<Project> expected, List<Project> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertProjectEquals(expected.get(i), actual.get(i));
    }
  }

  private void assertProjectEquals(Project expected, Project actual) {
    assertEquals(expected.getProjectId(), actual.getProjectId());
    assertEquals(expected.getProjectName(), actual.getProjectName());
  }
}
