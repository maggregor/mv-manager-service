package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ProjectController;
import com.achilio.mvm.service.controllers.requests.UpdateProjectRequest;
import com.achilio.mvm.service.controllers.responses.ProjectResponse;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
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
  private final ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks ProjectController controller;
  @Mock FetcherService mockedFetcherService;
  @Mock ProjectService mockedProjectService;

  @Test
  public void contextLoads() {
    assertThat(controller).isNotNull();
  }

  @Test
  public void getProject() throws JsonProcessingException {
    String expectedResponse1 =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"\",\"mvMaxPerTable\":20,\"analysisTimeframe\":30,\"activated\":false,\"automatic\":false}";
    Project project = new Project(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    DefaultFetchedProject fetchedProject =
        new DefaultFetchedProject(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    when(mockedFetcherService.fetchProject(any())).thenReturn(fetchedProject);
    when(mockedProjectService.findProjectOrCreate(any(), any())).thenReturn(project);
    ProjectResponse responseEntity = controller.getProject(TEST_PROJECT_ID1);
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse1, jsonResponse);

    String expectedResponse2 =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":20,\"analysisTimeframe\":30,\"activated\":false,\"automatic\":false}";
    project.setUsername("myEmail");
    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse2, jsonResponse);

    String expectedResponse3 =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":10,\"analysisTimeframe\":14,\"activated\":true,\"automatic\":true}";
    project.setAutomaticAvailable(true);
    project.setActivated(true);
    project.setAutomatic(true);
    project.setMvMaxPerTable(10);
    project.setAnalysisTimeframe(14);
    responseEntity = controller.getProject(TEST_PROJECT_ID1);
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse3, jsonResponse);
  }

  @Test
  public void getAllProject() throws Exception {
    String expectedResponse1 =
        "[{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":20,\"analysisTimeframe\":30,\"activated\":false,\"automatic\":false},{\"projectId\":\"other-project\",\"projectName\":\"Other Project\",\"username\":\"\",\"mvMaxPerTable\":10,\"analysisTimeframe\":14,\"activated\":true,\"automatic\":true}]";
    Project project1 = new Project(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    Project project2 = new Project(TEST_PROJECT_ID2, TEST_PROJECT_NAME2);
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
    when(mockedProjectService.findProjectOrCreate(TEST_PROJECT_ID1, TEST_PROJECT_NAME1))
        .thenReturn(project1);
    when(mockedProjectService.findProjectOrCreate(TEST_PROJECT_ID2, TEST_PROJECT_NAME2))
        .thenReturn(project2);
    List<ProjectResponse> responseEntity = controller.getAllProjects();
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse1, jsonResponse);
  }

  @Test
  public void updateProject() throws JsonProcessingException {
    Project project = new Project(TEST_PROJECT_ID1, TEST_PROJECT_NAME1);
    project.setAnalysisTimeframe(20);
    project.setMvMaxPerTable(10);
    project.setAutomaticAvailable(true);
    project.setAutomatic(true);
    UpdateProjectRequest payload = new UpdateProjectRequest(TEST_PROJECT_NAME1, true, 20, 10);
    when(mockedProjectService.updateProject(any(), any())).thenReturn(project);
    ProjectResponse responseEntity = controller.updateProject(TEST_PROJECT_ID1, payload);
    String expectedResponse =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"\",\"mvMaxPerTable\":10,\"analysisTimeframe\":20,\"activated\":false,\"automatic\":true}";
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse, jsonResponse);
  }
}
