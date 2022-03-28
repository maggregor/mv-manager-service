package com.achilio.mvm.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.entities.AOrganization.OrganizationType;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.achilio.mvm.service.services.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.services.oauth2.model.Userinfo;
import com.stripe.model.Product;
import java.util.Arrays;
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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

  private static final String TEST_PROJECT_ID1 = "achilio-dev";
  private static final String TEST_PROJECT_ID2 = "other-project";
  private static final String TEST_PROJECT_NAME1 = "Achilio Dev";
  private static final String TEST_PROJECT_NAME2 = "Other Project";
  private static final Project mockedProject1 = mock(Project.class);
  private static final Project mockedProject2 = mock(Project.class);
  private static final FetchedProject mockedFetchedProject = mock(FetchedProject.class);
  private static final List<Project> mockedActivatedProjects =
      Arrays.asList(mockedProject1, mockedProject2);
  private static final String TEST_DATASET_NAME1 = "nyc_trips";
  private static final String TEST_DATASET_NAME2 = "another_one";
  private static final String TEST_DATASET_NAME3 = "other_dataset";
  private static final String ORGANIZATION_ID = "organization/123456";
  private static final String ORGANIZATION_NAME = "achilio.com";
  private static final String STRIPE_CUSTOMER_ID = "cus_123456";
  private static final OrganizationType ORGANIZATION_TYPE = OrganizationType.ORGANIZATION;
  private static final AOrganization ORGANIZATION =
      new AOrganization(ORGANIZATION_ID, ORGANIZATION_NAME, STRIPE_CUSTOMER_ID, ORGANIZATION_TYPE);
  private static final ADataset mockedDataset1 = mock(ADataset.class);
  private static final ADataset mockedDataset2 = mock(ADataset.class);
  private final ADataset realDataset = new ADataset(mockedProject1, TEST_DATASET_NAME3);
  private final Map<String, String> productMetadata = new HashMap<>();
  private final Map<String, String> errorProductMetadata = new HashMap<>();
  private final Project realProject = new Project(TEST_PROJECT_ID2);
  @InjectMocks private ProjectService service;
  @Mock private ProjectRepository mockedProjectRepository;
  @Mock private DatasetRepository mockedDatasetRepository;
  @Mock private Product mockedProduct;
  @Mock private Product errorMockedProduct;
  @Mock private FetcherService mockedFetcherService;
  @Mock private GooglePublisherService mockedPublisherService;

  @Before
  public void setup() throws JsonProcessingException {
    when(mockedProject1.getProjectId()).thenReturn(TEST_PROJECT_ID1);
    when(mockedProject2.getProjectId()).thenReturn(TEST_PROJECT_ID2);
    when(mockedProjectRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(mockedProject1));
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID2))
        .thenReturn(Optional.of(realProject));
    when(mockedProjectRepository.findAllByActivated(true)).thenReturn(mockedActivatedProjects);
    when(mockedDataset1.isActivated()).thenReturn(true);
    when(mockedDataset2.isActivated()).thenReturn(false);
    when(mockedDatasetRepository.save(any(ADataset.class))).thenReturn(mockedDataset1);
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            mockedProject1.getProjectId(), TEST_DATASET_NAME1))
        .thenReturn(Optional.of(mockedDataset1));
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            mockedProject1.getProjectId(), TEST_DATASET_NAME2))
        .thenReturn(Optional.of(mockedDataset2));
    when(mockedDatasetRepository.findByProject_ProjectIdAndDatasetName(
            mockedProject1.getProjectId(), TEST_DATASET_NAME3))
        .thenReturn(Optional.of(realDataset));
    productMetadata.put("mv_max", "10");
    productMetadata.put("automatic_available", "true");
    when(mockedProduct.getMetadata()).thenReturn(productMetadata);
    when(errorMockedProduct.getMetadata()).thenReturn(errorProductMetadata);
    when(mockedFetcherService.getUserInfo()).thenReturn(new Userinfo().setEmail("myEmail"));
    when(mockedFetchedProject.getProjectId()).thenReturn(TEST_PROJECT_ID2);
    when(mockedFetchedProject.getName()).thenReturn(TEST_PROJECT_NAME2);
    when(mockedFetchedProject.getOrganization()).thenReturn(ORGANIZATION);
  }

  @Test
  public void createProject() {
    Project project = service.createProject(TEST_PROJECT_ID1);
    assertEquals(mockedProject1.getProjectId(), project.getProjectId());
  }

  @Test
  public void getProject() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(mockedProject1));
    Assert.assertNotNull(service.getProjectAsUser(TEST_PROJECT_ID1));
    Exception e =
        assertThrows(
            ProjectNotFoundException.class, () -> service.getProjectAsUser("unknown_project_id"));
    assertEquals("Project unknown_project_id not found", e.getMessage());
  }

  @Test
  public void findProject() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(mockedProject1));
    Assert.assertTrue(service.findProject(TEST_PROJECT_ID1).isPresent());
    assertFalse(service.findProject("unknown_project_id").isPresent());
  }

  @Test
  public void projectExists() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID1))
        .thenReturn(Optional.of(mockedProject1));
    assertTrue(service.projectExists(TEST_PROJECT_ID1));
    assertFalse(service.projectExists("unknown_project_id"));
  }

  @Test
  public void findOrCreateProject() {
    assertEquals(TEST_PROJECT_ID1, service.findProjectOrCreate(TEST_PROJECT_ID1).getProjectId());
    assertEquals(TEST_PROJECT_ID1, service.findProjectOrCreate(TEST_PROJECT_ID1).getProjectId());
  }

  @Test
  public void activateProject() {
    Project project = new Project(TEST_PROJECT_ID1);
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
    assertEquals("myEmail", project.getUsername());
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

  @Test
  public void createProjectFromFetchedProjectExists() {
    Project project1 = service.createProjectFromFetchedProject(mockedFetchedProject);
    assertEquals(TEST_PROJECT_ID2, project1.getProjectId());
    assertEquals(TEST_PROJECT_NAME2, project1.getProjectName());
    assertEquals(ORGANIZATION_NAME, project1.getOrganization().getName());
  }

  @Test
  public void createProjectFromFetchedProjectNotExists() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_ID2)).thenReturn(Optional.empty());
    Project project1 = service.createProjectFromFetchedProject(mockedFetchedProject);
    assertEquals(TEST_PROJECT_ID2, project1.getProjectId());
    assertEquals(TEST_PROJECT_NAME2, project1.getProjectName());
    assertEquals(ORGANIZATION_NAME, project1.getOrganization().getName());
  }
}
