package com.achilio.mvm.service;

import static com.achilio.mvm.service.AssertHelper.assertMVEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.controllers.MaterializedViewController;
import com.achilio.mvm.service.controllers.requests.MaterializedViewActionRequest;
import com.achilio.mvm.service.controllers.requests.MaterializedViewActionRequest.Action;
import com.achilio.mvm.service.controllers.requests.MaterializedViewRequest;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.entities.Project;
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
  MaterializedView mv1 =
      MockHelper.mvMock(1L, PROJECT1, DATASET1, TABLE1, STATEMENT1, MVStatus.NOT_APPLIED);
  MaterializedView mv2 =
      MockHelper.mvMock(2L, PROJECT1, DATASET1, TABLE1, STATEMENT2, MVStatus.NOT_APPLIED);
  MaterializedView mv3 =
      MockHelper.mvMock(3L, PROJECT2, DATASET1, TABLE1, STATEMENT1, MVStatus.NOT_APPLIED);
  MaterializedView mv4 =
      MockHelper.mvMock(4L, PROJECT2, DATASET2, TABLE1, STATEMENT1, MVStatus.NOT_APPLIED);
  Connection connection1 = MockHelper.connectionMock();
  Project project1 = MockHelper.projectMock(PROJECT1, connection1);
  @InjectMocks MaterializedViewController controller;
  @Mock MaterializedViewService mockedService;
  @Mock ProjectService mockedProjectService;

  @Before
  public void setup() {
    MockHelper.setupMockedAuthenticationContext();
    when(mockedProjectService.getProject(eq(PROJECT1), any())).thenReturn(project1);
    when(mockedProjectService.getProject(eq(UNKNOWN_PROJECT), any()))
        .thenThrow(new ProjectNotFoundException(UNKNOWN_PROJECT));
    when(mockedService.getAllMaterializedViews(eq(PROJECT1), any(), any(), any()))
        .thenReturn(Arrays.asList(mv1, mv2));
    when(mockedService.getAllMaterializedViews(eq(PROJECT1), eq(DATASET2), any(), any()))
        .thenReturn(Collections.emptyList());
    when(mockedService.getMaterializedView(1L, PROJECT1)).thenReturn(mv1);
    when(mockedService.getMaterializedView(2L, PROJECT1)).thenReturn(mv2);
    when(mockedService.getMaterializedView(99L, PROJECT1))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    when(mv3.getStatus()).thenReturn(MVStatus.NOT_APPLIED);
    when(mv3.getStatusReason()).thenReturn(MVStatusReason.ERROR);
    when(mv4.getStatus()).thenReturn(MVStatus.NOT_APPLIED);
    when(mv4.getStatusReason()).thenReturn(MVStatusReason.DELETED_BY_USER);
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
    assertTrue(mv1.isNotApplied());
    assertMVEquals(mv2, getMV2);
    assertFalse(mv2.isApplied());
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
  public void addMaterializedView() {
    when(mockedService.addMaterializedView(any(), any(), any(), any())).thenReturn(mv1);
    MaterializedViewRequest payload =
        new MaterializedViewRequest(PROJECT1, DATASET1, TABLE1, STATEMENT1);
    MaterializedView mv = controller.addMaterializedView(payload);
    assertMVEquals(mv1, mv);
  }

  @Test
  public void addMaterializedView__whenProjectNotFound_throwException() {
    MaterializedViewRequest payload =
        new MaterializedViewRequest(UNKNOWN_PROJECT, DATASET1, TABLE1, STATEMENT1);
    assertThrows(ProjectNotFoundException.class, () -> controller.addMaterializedView(payload));
  }

  @Test
  public void applyMaterializedView() {
    when(mockedService.applyMaterializedView(eq(3L), any(), any())).thenReturn(mv3);
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(PROJECT1, Action.APPLY);
    MaterializedView getMV1 = controller.actionOnMaterializedView(3L, payload);
    assertMVEquals(mv3, getMV1);
    assertEquals(MVStatus.NOT_APPLIED, getMV1.getStatus());
    assertEquals(MVStatusReason.ERROR, getMV1.getStatusReason());
  }

  @Test
  public void applyMaterializedView__whenProjectNotFound_throwException() {
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(UNKNOWN_PROJECT, Action.APPLY);
    assertThrows(
        ProjectNotFoundException.class, () -> controller.actionOnMaterializedView(1L, payload));
  }

  @Test
  public void applyMaterializedView__whenMVNotFound_throwException() {
    when(mockedService.applyMaterializedView(eq(99L), any(), any()))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(PROJECT1, Action.APPLY);
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.actionOnMaterializedView(99L, payload));
  }

  @Test
  public void unapplyMaterializedView() {
    when(mockedService.unapplyMaterializedView(eq(4L), any(), any())).thenReturn(mv4);
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(PROJECT1, Action.UNAPPLY);
    MaterializedView getMV1 = controller.actionOnMaterializedView(4L, payload);
    assertMVEquals(mv4, getMV1);
    assertEquals(MVStatus.NOT_APPLIED, getMV1.getStatus());
    assertEquals(MVStatusReason.DELETED_BY_USER, getMV1.getStatusReason());
  }

  @Test
  public void unapplyMaterializedView__whenProjectNotFound_throwException() {
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(UNKNOWN_PROJECT, Action.UNAPPLY);
    assertThrows(
        ProjectNotFoundException.class, () -> controller.actionOnMaterializedView(1L, payload));
  }

  @Test
  public void unapplyMaterializedView__whenMVNotFound_throwException() {
    when(mockedService.unapplyMaterializedView(eq(99L), any(), any()))
        .thenThrow(new MaterializedViewNotFoundException(99L));
    MaterializedViewActionRequest payload =
        new MaterializedViewActionRequest(PROJECT1, Action.UNAPPLY);
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.actionOnMaterializedView(99L, payload));
  }

  @Test
  public void deleteMaterializedView() {
    doNothing().when(mockedService).removeMaterializedView(1L, PROJECT1);
    controller.deleteMaterializedView(1L, PROJECT1);
    verify(mockedService, timeout(1000).times(1)).removeMaterializedView(1L, PROJECT1);
  }

  @Test
  public void deleteMaterializedView__whenMVNotFound_throwException() {
    doThrow(MaterializedViewNotFoundException.class)
        .when(mockedService)
        .removeMaterializedView(1L, PROJECT1);
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> controller.deleteMaterializedView(1L, PROJECT1));
    verify(mockedService, timeout(1000).times(1)).removeMaterializedView(1L, PROJECT1);
  }

  @Test
  public void deleteMaterializedView__whenProjectNotFound_throwException() {
    assertThrows(
        ProjectNotFoundException.class,
        () -> controller.deleteMaterializedView(1L, UNKNOWN_PROJECT));
    verify(mockedService, timeout(1000).times(0)).removeMaterializedView(1L, PROJECT1);
  }
}
