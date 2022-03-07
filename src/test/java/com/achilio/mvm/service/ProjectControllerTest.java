package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.ProjectController;
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
    Project project = new Project("achilio-dev");
    DefaultFetchedProject fetchedProject = new DefaultFetchedProject("achilio-dev", "Achilio Dev");
    when(mockedFetcherService.fetchProject(any())).thenReturn(fetchedProject);
    when(mockedProjectService.findProjectOrCreate(any())).thenReturn(project);
    ProjectResponse responseEntity = controller.getProject("achilio-dev");
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse1, jsonResponse);

    String expectedResponse2 =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":20,\"analysisTimeframe\":30,\"activated\":false,\"automatic\":false}";
    project.setUsername("myEmail");
    responseEntity = controller.getProject("achilio-dev");
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse2, jsonResponse);

    String expectedResponse3 =
        "{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":10,\"analysisTimeframe\":14,\"activated\":true,\"automatic\":true}";
    project.setAutomaticAvailable(true);
    project.setActivated(true);
    project.setAutomatic(true);
    project.setMvMaxPerTable(10);
    project.setAnalysisTimeframe(14);
    responseEntity = controller.getProject("achilio-dev");
    jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse3, jsonResponse);
  }

  @Test
  public void getAllProject() throws Exception {
    String expectedResponse1 =
        "[{\"projectId\":\"achilio-dev\",\"projectName\":\"Achilio Dev\",\"username\":\"myEmail\",\"mvMaxPerTable\":20,\"analysisTimeframe\":30,\"activated\":false,\"automatic\":false},{\"projectId\":\"achilio-main\",\"projectName\":\"Achilio Main\",\"username\":\"\",\"mvMaxPerTable\":10,\"analysisTimeframe\":14,\"activated\":true,\"automatic\":true}]";
    Project project1 = new Project("achilio-dev");
    Project project2 = new Project("achilio-main");
    project1.setUsername("myEmail");
    project2.setAutomaticAvailable(true);
    project2.setActivated(true);
    project2.setAutomatic(true);
    project2.setMvMaxPerTable(10);
    project2.setAnalysisTimeframe(14);
    DefaultFetchedProject fetchedProject1 = new DefaultFetchedProject("achilio-dev", "Achilio Dev");
    DefaultFetchedProject fetchedProject2 =
        new DefaultFetchedProject("achilio-main", "Achilio Main");
    when(mockedFetcherService.fetchAllProjects())
        .thenReturn(Arrays.asList(fetchedProject1, fetchedProject2));
    when(mockedFetcherService.fetchProject("achilio-dev")).thenReturn(fetchedProject1);
    when(mockedFetcherService.fetchProject("achilio-main")).thenReturn(fetchedProject2);
    when(mockedProjectService.findProjectOrCreate("achilio-dev")).thenReturn(project1);
    when(mockedProjectService.findProjectOrCreate("achilio-main")).thenReturn(project2);
    List<ProjectResponse> responseEntity = controller.getAllProjects();
    String jsonResponse = objectMapper.writeValueAsString(responseEntity);
    assertEquals(expectedResponse1, jsonResponse);
  }
}
