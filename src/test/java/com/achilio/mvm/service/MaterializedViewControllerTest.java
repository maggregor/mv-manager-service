package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.MaterializedViewController;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.exceptions.MaterializedViewNotFoundException;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import com.achilio.mvm.service.services.MaterializedViewService;
import com.achilio.mvm.service.services.ProjectService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaterializedViewControllerTest {

  private static final String PROJECT1 = "project1";
  private static final String PROJECT2 = "project2";
  private static final String UNKNOWN_PROJECT = "unknownProject";
  private static final String DATASET1 = "dataset1";
  private static final String DATASET2 = "dataset2";
  private static final String TABLE1 = "table1";
  private static final String STATEMENT1 = "SELECT 1";
  private static final String STATEMENT2 = "SELECT 2";
  MaterializedView mv1 = MockHelper.mvMock(1L, PROJECT1, DATASET1, TABLE1, STATEMENT1);
  MaterializedView mv2 = MockHelper.mvMock(2L, PROJECT1, DATASET1, TABLE1, STATEMENT2);
  MaterializedView mv3 = MockHelper.mvMock(3L, PROJECT2, DATASET1, TABLE1, STATEMENT1);
  MaterializedView mv4 = MockHelper.mvMock(4L, PROJECT2, DATASET2, TABLE1, STATEMENT1);

  @InjectMocks MaterializedViewController controller;
  @Mock MaterializedViewService mockedService;
  @Mock ProjectService mockedProjectService;

  @Before
  public void setup() {
    MockHelper.setupMockedAuthenticationContext();
    when(mockedProjectService.getProject(eq(UNKNOWN_PROJECT), any()))
        .thenThrow(new ProjectNotFoundException(UNKNOWN_PROJECT));
    when(mockedService.getAllMaterializedViews(eq(PROJECT1), any(), any(), any()))
        .thenReturn(Arrays.asList(mv1, mv2));
    when(mockedService.getAllMaterializedViews(eq(PROJECT1), eq(DATASET2), any(), any()))
        .thenReturn(Collections.emptyList());
    when(mockedService.getMaterializedView(1L)).thenReturn(mv1);
    when(mockedService.getMaterializedView(2L)).thenReturn(mv2);
    when(mockedService.getMaterializedView(99L))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    when(mv3.getStatus()).thenReturn(MVStatus.NOT_APPLIED);
    when(mv3.getStatusReason()).thenReturn(MVStatusReason.ERROR);
    when(mv4.getStatus()).thenReturn(MVStatus.NOT_APPLIED);
    when(mv4.getStatusReason()).thenReturn(MVStatusReason.DELETED);
  }

  @Test
  public void getAllMaterializedViews() {
    List<MaterializedView> mvList1 =
        controller.getAllMaterializedViews(PROJECT1, DATASET1, TABLE1, 1L);
    assertEquals(2, mvList1.size());

    List<MaterializedView> mvList2 =
        controller.getAllMaterializedViews(PROJECT1, DATASET2, null, null);
    assertEquals(0, mvList2.size());
  }

  @Test
  public void getAllMaterializedViews__whenProjectNotFound_throwException() {
    assertThrows(
        ProjectNotFoundException.class,
        () -> controller.getAllMaterializedViews(UNKNOWN_PROJECT, null, null, null));
  }

  @Test
  public void getMaterializedView() {
    MaterializedView getMV1 = controller.getMaterializedView(1L, PROJECT1);
    MaterializedView getMV2 = controller.getMaterializedView(2L, PROJECT1);
    assertMVEquals(mv1, getMV1);
    assertMVEquals(mv2, getMV2);
  }

  @Test
  public void getMaterializedView__whenProjectNotFound_throwException() {
    assertThrows(
        ProjectNotFoundException.class, () -> controller.getMaterializedView(1L, UNKNOWN_PROJECT));
  }

  @Test
  public void getMaterializedView__whenMVNotFound_throwException() {
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.getMaterializedView(99L, PROJECT1));
  }

  @Test
  public void applyMaterializedView() {
    when(mockedService.applyMaterializedView(3L)).thenReturn(mv3);
    MaterializedView getMV1 = controller.applyMaterializedView(3L, PROJECT1);
    assertMVEquals(mv3, getMV1);
    assertEquals(MVStatus.NOT_APPLIED, getMV1.getStatus());
    assertEquals(MVStatusReason.ERROR, getMV1.getStatusReason());
  }

  @Test
  public void applyMaterializedView__whenProjectNotFound_throwException() {
    assertThrows(
        ProjectNotFoundException.class,
        () -> controller.applyMaterializedView(1L, UNKNOWN_PROJECT));
  }

  @Test
  public void applyMaterializedView__whenMVNotFound_throwException() {
    when(mockedService.applyMaterializedView(99L))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.applyMaterializedView(99L, PROJECT1));
  }

  @Test
  public void deleteMaterializedView() {
    when(mockedService.deleteMaterializedView(4L)).thenReturn(mv4);
    MaterializedView getMV1 = controller.deleteMaterializedView(4L, PROJECT1);
    assertMVEquals(mv4, getMV1);
    assertEquals(MVStatus.NOT_APPLIED, getMV1.getStatus());
    assertEquals(MVStatusReason.DELETED, getMV1.getStatusReason());
  }

  @Test
  public void deleteMaterializedView__whenProjectNotFound_throwException() {
    assertThrows(
        ProjectNotFoundException.class,
        () -> controller.applyMaterializedView(1L, UNKNOWN_PROJECT));
  }

  @Test
  public void deleteMaterializedView__whenMVNotFound_throwException() {
    when(mockedService.deleteMaterializedView(99L))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.deleteMaterializedView(99L, PROJECT1));
  }

  private void assertMVEquals(MaterializedView expected, MaterializedView actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getProjectId(), actual.getProjectId());
    assertEquals(expected.getDatasetName(), actual.getDatasetName());
    assertEquals(expected.getTableName(), actual.getTableName());
    assertEquals(expected.getMvName(), actual.getMvName());
    assertEquals(expected.getMvUniqueName(), actual.getMvUniqueName());
    assertEquals(expected.getStatement(), actual.getStatement());
    assertEquals(expected.getStatementHashCode(), actual.getStatementHashCode());
  }
}
