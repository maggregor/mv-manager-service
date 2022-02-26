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
  }
}
