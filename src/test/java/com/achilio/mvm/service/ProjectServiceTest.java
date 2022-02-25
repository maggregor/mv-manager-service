package com.achilio.mvm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.repositories.ProjectRepository;
import com.achilio.mvm.service.services.ProjectService;
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

  private static final String TEST_PROJECT_NAME = "achilio-dev";
  private static final Project mockedProject = mock(Project.class);
  @InjectMocks private ProjectService service;
  @Mock private ProjectRepository repository;

  @Before
  public void setup() {
    when(mockedProject.getProjectId()).thenReturn(TEST_PROJECT_NAME);
    when(repository.save(Mockito.any(Project.class))).thenReturn(mockedProject);
  }

  @Test
  public void createProject() {
    Project project = service.createProject(TEST_PROJECT_NAME);
    assertEquals(mockedProject.getProjectId(), project.getProjectId());
  }

  @Test
  public void getProject() {
    when(repository.findByProjectId(TEST_PROJECT_NAME)).thenReturn(Optional.of(mockedProject));
    Assert.assertNotNull(service.getProject(TEST_PROJECT_NAME));
    Exception e =
        assertThrows(
            ProjectNotFoundException.class, () -> service.getProject("unknown_project_id"));
    assertEquals("Project unknown_project_id not found", e.getMessage());
  }

  @Test
  public void findProject() {
    when(repository.findByProjectId(TEST_PROJECT_NAME)).thenReturn(Optional.of(mockedProject));
    Assert.assertTrue(service.findProject(TEST_PROJECT_NAME).isPresent());
    Assert.assertFalse(service.findProject("unknown_project_id").isPresent());
  }

  @Test
  public void projectExists() {
    when(repository.findByProjectId(TEST_PROJECT_NAME)).thenReturn(Optional.of(mockedProject));
    assertTrue(service.projectExists(TEST_PROJECT_NAME));
    assertFalse(service.projectExists("unknown_project_id"));
  }

  @Test
  public void findOrCreateProject() {
    assertEquals(TEST_PROJECT_NAME, service.findProjectOrCreate(TEST_PROJECT_NAME).getProjectId());
    when(repository.findByProjectId(TEST_PROJECT_NAME)).thenReturn(Optional.of(mockedProject));
    assertEquals(TEST_PROJECT_NAME, service.findProjectOrCreate(TEST_PROJECT_NAME).getProjectId());
  }
}
