package com.achilio.mvm.service;

import com.achilio.mvm.service.controllers.AOrganizationController;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.services.AOrganizationService;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class ConnectionControllerTest {

  @InjectMocks AOrganizationController controller;
  @Mock AOrganizationService mockOrganizationService;

  @Before
  public void setup() {}

  @Test
  public void getAll() {
    List<AOrganization> organizationList = controller.getAllOrganizations();
  }
}
