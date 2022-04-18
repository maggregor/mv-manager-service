package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Connection;
import com.achilio.mvm.service.entities.FetcherStructJob;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.entities.ServiceAccountConnection;
import com.achilio.mvm.service.repositories.AColumnRepository;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import com.achilio.mvm.service.repositories.ConnectionRepository;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is an integration tests for the repositories classes
 *
 * <p>All repositories tests must go here, since the context will be instantiated only once
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IntegrationRepositoriesTest {

  private final String TEST_PROJECT_ID1 = "myProjectId";

  private final FetcherStructJob job5 = new FetcherStructJob(TEST_PROJECT_ID1);
  private final Connection connection1 = new ServiceAccountConnection("SA_JSON_CONTENT");
  private final Connection connection2 = new ServiceAccountConnection("SA_JSON_CONTENT");
  @Autowired private FetcherJobRepository fetcherJobRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ADatasetRepository datasetRepository;
  @Autowired private ATableRepository tableRepository;
  @Autowired private AColumnRepository columnRepository;
  @Autowired private ConnectionRepository connectionRepository;

  @Before
  public void setup() {
    fetcherJobRepository.save(job5);
    connection1.setTeamName("myTeam");
    connection2.setTeamName("myTeam");
  }

  @After
  public void cleanUp() {
    columnRepository.deleteAll();
    fetcherJobRepository.deleteAll();
  }

  @BeforeEach
  public void clear() {
    connectionRepository.deleteAll();
  }

  @Test
  public void connection_findAllByTeamName() {
    assertEquals(0, connectionRepository.findAllByTeamName("myTeam").size());
    connectionRepository.save(connection1);
    assertEquals(1, connectionRepository.findAllByTeamName("myTeam").size());
    connectionRepository.save(connection2);
    assertEquals(2, connectionRepository.findAllByTeamName("myTeam").size());
  }

  @Test
  @Transactional
  public void connection_deleteByIdAndTeamName() {
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.save(connection1);
    connectionRepository.save(connection2);
    assertFalse(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection1.getId(), "myTeam");
    assertFalse(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection2.getId(), "myTeam");
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
    connectionRepository.deleteByIdAndTeamName(connection1.getId(), "myTeam");
    assertTrue(connectionRepository.findAllByTeamName("myTeam").isEmpty());
  }

  @Test
  public void column_findAllByProject() {
    String PROJECT_ID1 = "project-id1";
    String PROJECT_ID2 = "project-id2";
    String PROJECT_NAME1 = "Project 1";
    String PROJECT_NAME2 = "Project 2";
    String DATASET_NAME1 = "myDataset1";
    String DATASET_NAME2 = "myDataset2";
    String TABLE_NAME1 = "myTable1";
    String COLUMN_NAME1 = "myColumn1";
    String COLUMN_TYPE1 = "columnType1";

    Project project1 = new Project(PROJECT_ID1, PROJECT_NAME1);
    Project project2 = new Project(PROJECT_ID2, PROJECT_NAME2);
    ADataset dataset1 = new ADataset(project1, DATASET_NAME1);
    ADataset dataset2 = new ADataset(project2, DATASET_NAME2);
    ATable table1 = new ATable(project1, dataset1, TABLE_NAME1);
    ATable table2 = new ATable(project2, dataset1, TABLE_NAME1);
    AColumn column1 = new AColumn(job5, table1, COLUMN_NAME1, COLUMN_TYPE1);
    AColumn column2 = new AColumn(job5, table2, COLUMN_NAME1, COLUMN_TYPE1);
    projectRepository.save(project1);
    projectRepository.save(project2);
    datasetRepository.save(dataset1);
    datasetRepository.save(dataset2);
    tableRepository.save(table1);
    tableRepository.save(table2);
    columnRepository.save(column1);
    columnRepository.save(column2);

    List<AColumn> columns = columnRepository.findAllByTable_Project_ProjectId(PROJECT_ID1);
    assertEquals(1, columns.size());
    assertEquals(
        String.format("%s.%s.%s#%s", PROJECT_ID1, DATASET_NAME1, TABLE_NAME1, COLUMN_NAME1),
        columns.get(0).getColumnId());
  }
}
