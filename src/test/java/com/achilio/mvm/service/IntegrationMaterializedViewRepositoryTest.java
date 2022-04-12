package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.FindMVJob;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.repositories.FindMVJobRepository;
import com.achilio.mvm.service.repositories.MaterializedViewRepository;
import com.achilio.mvm.service.visitors.ATableId;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IntegrationMaterializedViewRepositoryTest {

  private static final String STATEMENT1 = "SELECT 1";
  private static final String STATEMENT2 = "SELECT 2";
  private static final String STATEMENT3 = "SELECT 3";
  private static final String STATEMENT4 = "SELECT 4";
  private static final String STATEMENT5 = "SELECT 5";
  private static final String STATEMENT6 = "SELECT 6";
  private static final String STATEMENT7 = "SELECT 7";
  private static final String PROJECT_ID1 = "my-project-1";
  private static final String PROJECT_ID2 = "my-project-2";
  private static final String DATASET_NAME1 = "myDataset1";
  private static final String DATASET_NAME2 = "myDataset2";
  private static final String DATASET_NAME3 = "myDataset3";
  private static final String TABLE_NAME1 = "myTable1";
  private static final String TABLE_NAME2 = "myTable2";
  private static final String TABLE_NAME3 = "myTable3";
  private static final String TABLE_NAME4 = "myTable4";
  private static final String TABLE_NAME5 = "myTable5";
  private static final String TABLE_NAME6 = "myTable6";
  private static final ATableId TABLE_ID1 = ATableId.of(PROJECT_ID1, DATASET_NAME1, TABLE_NAME1);
  private static final ATableId TABLE_ID2 = ATableId.of(PROJECT_ID1, DATASET_NAME1, TABLE_NAME2);
  private static final ATableId TABLE_ID3 = ATableId.of(PROJECT_ID1, DATASET_NAME2, TABLE_NAME2);
  private static final ATableId TABLE_ID4 = ATableId.of(PROJECT_ID2, DATASET_NAME1, TABLE_NAME1);
  private static final ATableId TABLE_ID5 = ATableId.of(PROJECT_ID2, DATASET_NAME1, TABLE_NAME3);
  private static final ATableId TABLE_ID6 = ATableId.of(PROJECT_ID2, DATASET_NAME2, TABLE_NAME5);
  private static final ATableId TABLE_ID7 = ATableId.of(PROJECT_ID2, DATASET_NAME3, TABLE_NAME1);
  private static MaterializedView mv1;
  private static MaterializedView mv2;
  private static MaterializedView mv3;
  private static MaterializedView mv4;
  private static MaterializedView mv5;
  private static MaterializedView mv6;
  private static MaterializedView mv7;
  private static MaterializedView mv8;
  private static MaterializedView mv9;
  private static MaterializedView mv10;
  private static MaterializedView mv11;
  private static MaterializedView mv12;
  private static FindMVJob job1;
  private static FindMVJob job2;

  @Autowired private MaterializedViewRepository repository;
  @Autowired private FindMVJobRepository jobRepository;

  @Before
  public void setup() {
    FindMVJob newJob1 = new FindMVJob(PROJECT_ID1, 7);
    FindMVJob newJob2 = new FindMVJob(PROJECT_ID2, 7);
    job1 = jobRepository.save(newJob1);
    job2 = jobRepository.save(newJob2);
    MaterializedView newMv1 = new MaterializedView(job1, TABLE_ID1, STATEMENT1);
    MaterializedView newMv2 = new MaterializedView(job1, TABLE_ID1, STATEMENT2);
    MaterializedView newMv3 = new MaterializedView(job1, TABLE_ID2, STATEMENT1);
    MaterializedView newMv4 = new MaterializedView(job1, TABLE_ID3, STATEMENT1);
    MaterializedView newMv5 = new MaterializedView(job1, TABLE_ID4, STATEMENT1);
    MaterializedView newMv6 = new MaterializedView(job1, TABLE_ID5, STATEMENT1);
    MaterializedView newMv7 = new MaterializedView(job2, TABLE_ID1, STATEMENT3);
    MaterializedView newMv8 = new MaterializedView(job2, TABLE_ID6, STATEMENT1);
    MaterializedView newMv9 = new MaterializedView(job2, TABLE_ID2, STATEMENT2);
    MaterializedView newMv10 = new MaterializedView(job2, TABLE_ID7, STATEMENT1);
    MaterializedView newMv11 = new MaterializedView(job2, TABLE_ID3, STATEMENT2);
    MaterializedView newMv12 = new MaterializedView(job2, TABLE_ID1, STATEMENT4);

    mv1 = repository.save(newMv1);
    mv2 = repository.save(newMv2);
    mv3 = repository.save(newMv3);
    mv4 = repository.save(newMv4);
    mv5 = repository.save(newMv5);
    mv6 = repository.save(newMv6);
    mv7 = repository.save(newMv7);
    mv8 = repository.save(newMv8);
    mv9 = repository.save(newMv9);
    mv10 = repository.save(newMv10);
    mv11 = repository.save(newMv11);
    mv12 = repository.save(newMv12);
  }

  @After
  public void clean() {
    repository.deleteAll();
  }

  @Test
  public void findAllMaterializedViews() {
    List<MaterializedView> mvList1 =
        repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            PROJECT_ID1, DATASET_NAME1, TABLE_NAME1, job1.getId());
    // mv1, mv2
    assertEquals(2, mvList1.size());
    assertEquals(mv1.getId(), mvList1.get(0).getId());
    assertEquals(mv2.getId(), mvList1.get(1).getId());

    List<MaterializedView> mvList2 =
        repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            PROJECT_ID1, DATASET_NAME1, null, job1.getId());
    // mv1, mv2, mv3
    assertEquals(3, mvList2.size());
    assertEquals(mv1.getId(), mvList2.get(0).getId());
    assertEquals(mv2.getId(), mvList2.get(1).getId());
    assertEquals(mv3.getId(), mvList2.get(2).getId());

    List<MaterializedView> mvList3 =
        repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            PROJECT_ID1, DATASET_NAME1, TABLE_NAME1, null);
    // mv1, mv2, mv7, mv12
    assertEquals(4, mvList3.size());
    assertEquals(mv1.getId(), mvList3.get(0).getId());
    assertEquals(mv2.getId(), mvList3.get(1).getId());
    assertEquals(mv7.getId(), mvList3.get(2).getId());
    assertEquals(mv12.getId(), mvList3.get(3).getId());

    List<MaterializedView> mvList4 =
        repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            PROJECT_ID1, null, null, null);
    // mv1, mv2, mv3, mv4, mv7, mv9, mv11, mv12
    assertEquals(8, mvList4.size());
    assertEquals(mv1.getId(), mvList4.get(0).getId());
    assertEquals(mv2.getId(), mvList4.get(1).getId());
    assertEquals(mv3.getId(), mvList4.get(2).getId());
    assertEquals(mv4.getId(), mvList4.get(3).getId());
    assertEquals(mv7.getId(), mvList4.get(4).getId());
    assertEquals(mv9.getId(), mvList4.get(5).getId());
    assertEquals(mv11.getId(), mvList4.get(6).getId());
    assertEquals(mv12.getId(), mvList4.get(7).getId());

    List<MaterializedView> mvList5 =
        repository.findAllByProjectIdAndDatasetNameAndTableNameAndLastJob_Id(
            null, null, null, null);
    // projectId is required
    assertEquals(0, mvList5.size());
  }

  @Test
  public void findByIdAndProjectId() {
    Optional<MaterializedView> findMv1 = repository.findByIdAndProjectId(mv1.getId(), PROJECT_ID1);
    assertTrue(findMv1.isPresent());
    Optional<MaterializedView> findMv2 = repository.findByIdAndProjectId(mv2.getId(), PROJECT_ID2);
    assertFalse(findMv2.isPresent());
    Optional<MaterializedView> findMv3 = repository.findByIdAndProjectId(-1L, PROJECT_ID2);
    assertFalse(findMv3.isPresent());
  }

  @Test
  public void findByMvUniqueName() {
    Optional<MaterializedView> findMv1 = repository.findByMvUniqueName(mv1.getMvUniqueName());
    assertTrue(findMv1.isPresent());
    Optional<MaterializedView> findMv2 = repository.findByMvUniqueName(mv2.getMvUniqueName());
    assertTrue(findMv2.isPresent());
    Optional<MaterializedView> findMv3 = repository.findByMvUniqueName("thisNameDoesNotExists");
    assertFalse(findMv3.isPresent());
  }
}
