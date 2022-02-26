package com.achilio.mvm.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Dataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.repositories.DatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.GooglePublisherService;
import com.achilio.mvm.service.services.ProjectService;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

  private static final String TEST_PROJECT_NAME1 = "achilio-dev";
  private static final String TEST_PROJECT_NAME2 = "other-project";
  private static final Project mockedProject1 = mock(Project.class);
  private static final Project mockedProject2 = mock(Project.class);
  private static final List<Project> mockedActivatedProjects =
      Arrays.asList(mockedProject1, mockedProject2);
  private static final String TEST_DATASET_NAME1 = "nyc_trips";
  private static final String TEST_DATASET_NAME2 = "other_dataset";
  private static final Dataset mockedDataset1 = mock(Dataset.class);
  private final Dataset realDataset = new Dataset(mockedProject1, TEST_DATASET_NAME2);
  private final Map<String, String> productMetadata = new HashMap<>();
  private final Map<String, String> errorProductMetadata = new HashMap<>();
  private final Project realProject = new Project(TEST_PROJECT_NAME2);
  @InjectMocks private ProjectService service;
  @Mock private ProjectRepository mockedProjectRepository;
  @Mock private DatasetRepository mockedDatasetRepository;
  @Mock private GooglePublisherService mockedPublisherService;
  @Mock private Product mockedProduct;
  @Mock private Product errorMockedProduct;
  @Mock private FetcherService mockedFetcherService;

  @Before
  public void setup() {
    when(mockedProject1.getProjectId()).thenReturn(TEST_PROJECT_NAME1);
    when(mockedProject2.getProjectId()).thenReturn(TEST_PROJECT_NAME2);

    when(mockedProjectRepository.save(Mockito.any(Project.class))).thenReturn(mockedProject1);
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_NAME1))
        .thenReturn(Optional.of(mockedProject1));
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_NAME2))
        .thenReturn(Optional.of(realProject));
    when(mockedProjectRepository.findAllByActivated(true)).thenReturn(mockedActivatedProjects);
    when(mockedDatasetRepository.save(Mockito.any(Dataset.class))).thenReturn(mockedDataset1);
    when(mockedDatasetRepository.findByProjectAndDatasetName(mockedProject1, TEST_DATASET_NAME1)).thenReturn(Optional.of(mockedDataset1));
    when(mockedDatasetRepository.findByProjectAndDatasetName(mockedProject1, TEST_DATASET_NAME2)).thenReturn(Optional.of(realDataset));
    productMetadata.put("mv_max", "10");
    productMetadata.put("automatic_available", "true");
    when(mockedProduct.getMetadata()).thenReturn(productMetadata);
    when(errorMockedProduct.getMetadata()).thenReturn(errorProductMetadata);
    when(mockedFetcherService.getUserInfo()).thenReturn(new Userinfo().setEmail("myEmail"));
  }

  @Test
  public void createProject() {
    Project project = service.createProject(TEST_PROJECT_NAME1);
    assertEquals(mockedProject1.getProjectId(), project.getProjectId());
  }

  @Test
  public void getProject() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_NAME1))
        .thenReturn(Optional.of(mockedProject1));
    Assert.assertNotNull(service.getProject(TEST_PROJECT_NAME1));
    Exception e =
        assertThrows(
            ProjectNotFoundException.class, () -> service.getProject("unknown_project_id"));
    assertEquals("Project unknown_project_id not found", e.getMessage());
  }

  @Test
  public void findProject() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_NAME1))
        .thenReturn(Optional.of(mockedProject1));
    Assert.assertTrue(service.findProject(TEST_PROJECT_NAME1).isPresent());
    assertFalse(service.findProject("unknown_project_id").isPresent());
  }

  @Test
  public void projectExists() {
    when(mockedProjectRepository.findByProjectId(TEST_PROJECT_NAME1))
        .thenReturn(Optional.of(mockedProject1));
    assertTrue(service.projectExists(TEST_PROJECT_NAME1));
    assertFalse(service.projectExists("unknown_project_id"));
  }

  @Test
  public void findOrCreateProject() {
    assertEquals(
        TEST_PROJECT_NAME1, service.findProjectOrCreate(TEST_PROJECT_NAME1).getProjectId());
    assertEquals(
        TEST_PROJECT_NAME1, service.findProjectOrCreate(TEST_PROJECT_NAME1).getProjectId());
  }

  @Test
  public void activateProject() {
    Project project = new Project("myProject");
    assertFalse(project.isActivated());
    service.activateProject(project);
    assertTrue(project.isActivated());
  }

  @Test
  public void updateProject() {
    Integer analysisTimeFrame = 14;
    Integer mvMaxPerTable = 12;
    Project project =
        service.updateProject(TEST_PROJECT_NAME2, false, analysisTimeFrame, mvMaxPerTable);
    assertEquals(TEST_PROJECT_NAME2, project.getProjectId());
    assertFalse(project.isAutomatic());
    assertEquals(14, project.getAnalysisTimeframe());
    assertEquals(12, project.getMvMaxPerTable());
    project.setAutomaticAvailable(true);
    project = service.updateProject(TEST_PROJECT_NAME2, true, analysisTimeFrame, mvMaxPerTable);
    assertTrue(project.isAutomatic());
    assertEquals("myEmail", project.getUsername());
  }

  @Test
  public void deactivateProject() {
    Project project = new Project("myProject");
    project.setAutomaticAvailable(true);
    project.setActivated(true);
    project.setAutomatic(true);
    assertTrue(project.isActivated());
    assertTrue(project.isAutomatic());
    service.deactivateProject(project);
    assertFalse(project.isActivated());
    assertFalse(project.isAutomatic());
    //    assertFalse(project.isAutomaticAvailable());
    //    assertEquals(0, project.getMvMaxPerTableLimit());
  }

  @Test
  public void updateMvMaxPerTableLimit() {
    Project project = new Project("myProject");
    assertEquals(20, project.getMvMaxPerTable());
    assertEquals(20, project.getMvMaxPerTableLimit());
    service.updateMvMaxPerTableLimit(project, 10);
    assertEquals(10, project.getMvMaxPerTable());
    assertEquals(10, project.getMvMaxPerTableLimit());
    service.updateMvMaxPerTableLimit(project, 12);
    assertEquals(10, project.getMvMaxPerTable());
    assertEquals(12, project.getMvMaxPerTableLimit());
  }

  @Test
  public void updateProjectAutomaticAvailable() {
    Project project = new Project("myProject");
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
    Project project = new Project("myProject");
    assertFalse(project.isAutomaticAvailable());
    assertEquals(20, project.getMvMaxPerTableLimit());
    service.updatePlanSettings(project, mockedProduct);
    assertTrue(project.isAutomaticAvailable());
    assertEquals(10, project.getMvMaxPerTableLimit());
    service.updatePlanSettings(project, errorMockedProduct);
    assertTrue(project.isAutomaticAvailable());
    assertEquals(10, project.getMvMaxPerTableLimit());
  }

//  @Test
//  public void updateDataset() {
//    Dataset dataset = new Dataset(mockedProject1, TEST_DATASET_NAME2);
//    service.updateDataset(TEST_DATASET_NAME2);
//  }
}
