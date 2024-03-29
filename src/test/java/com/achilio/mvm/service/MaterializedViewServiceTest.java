package com.achilio.mvm.service;

import static com.achilio.mvm.service.AssertHelper.assertMVEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.entities.MaterializedView.MVStatus;
import com.achilio.mvm.service.entities.MaterializedView.MVStatusReason;
import com.achilio.mvm.service.exceptions.MaterializedViewAppliedException;
import com.achilio.mvm.service.exceptions.MaterializedViewNotFoundException;
import com.achilio.mvm.service.repositories.MaterializedViewRepository;
import com.achilio.mvm.service.services.FetcherService;
import com.achilio.mvm.service.services.MaterializedViewService;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaterializedViewServiceTest {

  private static final String PROJECT1 = "project1";
  private static final String PROJECT2 = "project2";
  private static final String DATASET1 = "dataset1";
  private static final String DATASET2 = "dataset2";
  private static final String UNKNOWN_DATASET = "unknownDataset";
  private static final String TABLE1 = "table1";
  private static final String STATEMENT1 = "SELECT 1";
  private static final String STATEMENT2 = "SELECT 2";
  private static final FindMVJob mockJob = MockHelper.findMVJobMock();
  private static final MaterializedView realMv =
      new MaterializedView(mockJob, ATableId.of(PROJECT1, DATASET1, TABLE1), STATEMENT1);
  MaterializedView mv1 =
      MockHelper.mvMock(1L, PROJECT1, DATASET1, TABLE1, STATEMENT1, MVStatus.NOT_APPLIED);
  MaterializedView mv2 =
      MockHelper.mvMock(2L, PROJECT1, DATASET1, TABLE1, STATEMENT2, MVStatus.NOT_APPLIED);
  MaterializedView mv3 =
      MockHelper.mvMock(3L, PROJECT2, DATASET1, TABLE1, STATEMENT1, MVStatus.APPLIED);
  MaterializedView mv4 =
      MockHelper.mvMock(4L, PROJECT2, DATASET2, TABLE1, STATEMENT1, MVStatus.OUTDATED);
  List<MaterializedView> allMvs = Arrays.asList(mv1, mv2, mv3, mv4);
  Connection connection = MockHelper.connectionMock();

  @InjectMocks MaterializedViewService service;
  @Mock MaterializedViewRepository mockedRepository;
  @Mock FetcherService mockedFetcherService;

  @Before
  public void setup() {
    when(mockedRepository.findByIdAndProjectId(1L, PROJECT1)).thenReturn(Optional.ofNullable(mv1));
    when(mockedRepository.findByIdAndProjectId(2L, PROJECT1)).thenReturn(Optional.ofNullable(mv2));
    when(mockedRepository.findByIdAndProjectId(3L, PROJECT2)).thenReturn(Optional.ofNullable(mv3));
    when(mockedRepository.findByIdAndProjectId(4L, PROJECT2)).thenReturn(Optional.ofNullable(mv4));
    when(mockedRepository.findByIdAndProjectId(99L, PROJECT1)).thenReturn(Optional.empty());
    when(mockedRepository.save(any())).then(returnsFirstArg());
    doNothing().when(mockedRepository).delete(any());
    doNothing().when(mockedFetcherService).createMaterializedView(any(), any());
    doNothing().when(mockedFetcherService).deleteMaterializedView(any(), any());
  }

  @Test
  public void getMaterializedView() {
    MaterializedView getMv1 = service.getMaterializedView(1L, PROJECT1);
    MaterializedView getMv2 = service.getMaterializedView(2L, PROJECT1);
    MaterializedView getMv3 = service.getMaterializedView(3L, PROJECT2);
    MaterializedView getMv4 = service.getMaterializedView(4L, PROJECT2);
    assertMVEquals(mv1, getMv1);
    assertMVEquals(mv2, getMv2);
    assertMVEquals(mv3, getMv3);
    assertMVEquals(mv4, getMv4);
  }

  @Test
  public void getMaterializedView__whenNotExists_throwException() {
    assertThrows(
        MaterializedViewNotFoundException.class, () -> service.getMaterializedView(99L, PROJECT1));
    assertThrows(
        MaterializedViewNotFoundException.class, () -> service.getMaterializedView(3L, PROJECT1));
  }

  @Test
  public void getAllMaterializedViews() {
    when(mockedRepository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            any(), any(), any(), any()))
        .thenReturn(Arrays.asList(mv1, mv2, mv3));
    List<MaterializedView> mvList1 =
        service.getAllMaterializedViews(PROJECT1, DATASET1, TABLE1, null);
    assertEquals(3, mvList1.size());
    assertMVEquals(mv1, mvList1.get(0));
    assertMVEquals(mv2, mvList1.get(1));
    assertMVEquals(mv3, mvList1.get(2));

    when(mockedRepository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            any(), eq(UNKNOWN_DATASET), any(), any()))
        .thenReturn(Collections.emptyList());
    List<MaterializedView> mvList2 =
        service.getAllMaterializedViews(PROJECT1, UNKNOWN_DATASET, null, null);
    assertEquals(0, mvList2.size());
  }

  @Test
  public void addMaterializedView() {
    MaterializedView mv = service.addMaterializedView(PROJECT1, DATASET1, TABLE1, STATEMENT1);
    assertEquals(PROJECT1, mv.getProjectId());
    assertEquals(DATASET1, mv.getDatasetName());
    assertEquals(TABLE1, mv.getTableName());
    assertEquals(STATEMENT1, mv.getStatement());
    assertEquals(MVStatus.NOT_APPLIED, mv.getStatus());
    assertEquals(MVStatusReason.WAITING_APPROVAL, mv.getStatusReason());
    assertEquals("1974197773", mv.getStatementHashCode());
    assertEquals(TABLE1 + "_achilio_mv_" + 1974197773, mv.getMvName());
    assertEquals(String.join("-", PROJECT1, DATASET1, TABLE1, "1974197773"), mv.getMvUniqueName());
  }

  @Test
  public void applyMaterializedView() {
    when(mockedRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.of(realMv));
    MaterializedView appliedMv1 = service.applyMaterializedView(1L, PROJECT1, connection);
    assertEquals(MVStatus.APPLIED, appliedMv1.getStatus());
    assertNull(appliedMv1.getStatusReason());
  }

  @Test
  public void applyMaterializedView__whenMVNotFound_throwException() {
    when(mockedRepository.findByIdAndProjectId(1L, PROJECT2)).thenReturn(Optional.empty());
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> service.applyMaterializedView(1L, PROJECT2, connection));
    verify(mockedFetcherService, timeout(1000).times(0)).createMaterializedView(any(), any());
  }

  @Test
  public void applyMaterializedView__whenCreateFails_checkStatuses() {
    when(mockedRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.of(realMv));
    doThrow(new RuntimeException()).when(mockedFetcherService).createMaterializedView(any(), any());
    MaterializedView appliedMv1 = service.applyMaterializedView(1L, PROJECT1, connection);
    assertEquals(MVStatus.NOT_APPLIED, appliedMv1.getStatus());
    assertEquals(MVStatusReason.ERROR_DURING_CREATION, appliedMv1.getStatusReason());
  }

  @Test
  public void unapplyMaterializedView() {
    when(mockedRepository.findByIdAndProjectId(1L, PROJECT1)).thenReturn(Optional.of(realMv));
    doNothing().when(mockedFetcherService).deleteMaterializedView(any(), any());
    MaterializedView deletedMv1 = service.unapplyMaterializedView(1L, PROJECT1, connection);
    assertEquals(MVStatus.NOT_APPLIED, deletedMv1.getStatus());
    assertEquals(MVStatusReason.DELETED_BY_USER, deletedMv1.getStatusReason());
  }

  @Test
  public void unapplyMaterializedView__whenMVNotFound_throwException() {
    when(mockedRepository.findByIdAndProjectId(1L, PROJECT2)).thenReturn(Optional.empty());
    assertThrows(
        MaterializedViewNotFoundException.class,
        () -> service.unapplyMaterializedView(1L, PROJECT2, connection));
    verify(mockedFetcherService, timeout(1000).times(0)).deleteMaterializedView(any(), any());
  }

  @Test
  public void unapplyMaterializedView__whenDeleteFails_checkStatuses() {
    when(mockedRepository.findByIdAndProjectId(any(), any())).thenReturn(Optional.of(realMv));
    doThrow(new RuntimeException()).when(mockedFetcherService).deleteMaterializedView(any(), any());
    MaterializedView deletedMv1 = service.unapplyMaterializedView(1L, PROJECT1, connection);
    assertEquals(MVStatus.UNKNOWN, deletedMv1.getStatus());
    assertEquals(MVStatusReason.ERROR_DURING_DELETION, deletedMv1.getStatusReason());
  }

  @Test
  public void removeMaterializedView() {
    service.removeMaterializedView(1L, PROJECT1);
    verify(mockedRepository, Mockito.timeout(1000).times(1)).delete(any());
  }

  @Test
  public void removeMaterializedView__whenNotExists_doNothing() {
    service.removeMaterializedView(99L, PROJECT1);
    verify(mockedRepository, Mockito.timeout(1000).times(0)).delete(any());
  }

  @Test
  public void removeMaterializedView__whenMVApplied_throwException() {
    when(mv2.getStatus()).thenReturn(MVStatus.APPLIED);
    assertThrows(
        MaterializedViewAppliedException.class, () -> service.removeMaterializedView(2L, PROJECT1));
  }

  @Test
  public void mvExists() {
    when(mockedRepository.findByMvUniqueName(mv1.getMvUniqueName()))
        .thenReturn(Optional.ofNullable(mv1));
    when(mockedRepository.findByMvUniqueName(mv2.getMvUniqueName()))
        .thenReturn(Optional.ofNullable(mv2));
    when(mockedRepository.findByMvUniqueName(mv3.getMvUniqueName())).thenReturn(Optional.empty());
    assertTrue(service.mvExists(mv1));
    assertTrue(service.mvExists(mv2));
    assertFalse(service.mvExists(mv3));
  }

  @Test
  public void deleteOld() {
    service.deleteOld(allMvs);
    verify(mockedRepository, timeout(1000).times(2)).delete(any());
  }

  @Test
  public void flagOutdated() {
    service.flagOutdated(allMvs);
    verify(mockedRepository, timeout(1000).times(2)).save(any());
  }

  @Test
  public void saveAllMaterializedViews() {
    service.saveAllMaterializedViews(allMvs);
    verify(mockedRepository, timeout(1000).times(1)).saveAll(any());
  }
}
