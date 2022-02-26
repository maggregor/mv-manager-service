package com.achilio.mvm.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.achilio.mvm.service.configuration.AccessTokenInterceptor;
import com.achilio.mvm.service.configuration.GoogleProjectInterceptor;
import com.achilio.mvm.service.controllers.ProjectController;
import com.achilio.mvm.service.databases.entities.DefaultFetchedProject;
import com.achilio.mvm.service.services.FetcherService;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

  @MockBean AccessTokenInterceptor accessTokenInterceptor;
  @MockBean GoogleProjectInterceptor googleProjectInterceptor;
  @MockBean FetcherService mockedFetcherService;
  @Autowired private ProjectController controller;
  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void initTest() throws IOException {
    when(accessTokenInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    when(googleProjectInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    // other stuff
  }

  @Test
  public void contextLoads() {
    assertThat(controller).isNotNull();
  }

  // WIP: Not working
  @Test
  @Ignore
  public void shouldReturnDefaultMessage() throws Exception {
    when(mockedFetcherService.fetchProject("achilio-dev"))
        .thenReturn(new DefaultFetchedProject("achilio-dev", "Achilio Dev"));
    this.mockMvc
        .perform(get("/api/v1/project/{projectId}", "achilio-dev"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Hello, World")));
  }
}
