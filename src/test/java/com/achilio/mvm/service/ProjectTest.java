package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectTest {

  @Test
  public void simpleValidation() {
    Project project = new Project("myProject");
    assertEquals("myProject", project.getProjectId());
    project.setActivated(true);
    assertTrue(project.isActivated());
    project.setActivated(false);
    assertFalse(project.isActivated());
    assertFalse(project.isAutomatic());
    project.setAutomatic(true);
    assertTrue(project.isAutomatic());
    project.setUsername("myUser");
    assertEquals("myUser", project.getUsername());
  }

  @Test
  public void simpleValidationNull() {
    Project project = new Project();
    assertNull(project.getProjectId());
    assertNull(project.getId());
  }

  @Test
  public void projectPlanSettingsSimpleValidation() {
    Project project = new Project();
    assertEquals(false, project.isAutomaticAvailable());
    project.setAutomaticAvailable(true);
    assertEquals(true, project.isAutomaticAvailable());
    project.setAutomaticAvailable(false);
    assertEquals(false, project.isAutomaticAvailable());
    project.setAutomaticAvailable(null);
    assertEquals(false, project.isAutomaticAvailable());

    assertEquals(Integer.valueOf(20), project.getMvMaxPerTableLimit());
    project.setMvMaxPerTableLimit(15);
    assertEquals(Integer.valueOf(15), project.getMvMaxPerTableLimit());
    project.setMvMaxPerTableLimit(null);
    assertEquals(Integer.valueOf(15), project.getMvMaxPerTableLimit());
  }

  @Test
  public void projectSettingsInvalidMvMax() {
    Project project = new Project("projectId");
    project.setMvMaxPerTable(15);
    assertEquals(Integer.valueOf(15), project.getMvMaxPerTable());
    project.setMvMaxPerTableLimit(10);
    assertEquals(Integer.valueOf(10), project.getMvMaxPerTable());
    try {
      project.setMvMaxPerTable(12);
    } catch (IllegalArgumentException e) {
      assertEquals("Cannot set max MV per table to 12. Limit is 10", e.getMessage());
    } finally {
      assertEquals(Integer.valueOf(10), project.getMvMaxPerTable());
    }
    project.setMvMaxPerTable(8);
    assertEquals(Integer.valueOf(8), project.getMvMaxPerTable());
  }

  @Test
  public void projectSettingsInvalidAutomatic() {
    Project project = new Project("projectId");
    assertEquals(false, project.isAutomatic());
    project.setAutomaticAvailable(true);
    project.setAutomatic(true);
    assertEquals(true, project.isAutomatic());
    project.setAutomaticAvailable(false);
    assertEquals(false, project.isAutomatic());
    project.setAutomatic(false);
    assertEquals(false, project.isAutomatic());
    try {
      project.setAutomatic(true);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "Cannot set project to automatic mode. Automatic mode is not available on this project",
          e.getMessage());
    } finally {
      assertEquals(false, project.isAutomatic());
    }
    project.setAutomatic(false);
    assertEquals(false, project.isAutomatic());
  }
}
