package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.Project;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ProjectRepository;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IntegrationDatasetRepositoryTest {

  private final String PROJECT_ID = "myProjectId";

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ADatasetRepository datasetRepository;

  @Before
  public void setup() {
    Project project = new Project(PROJECT_ID);
    projectRepository.save(project);
    ADataset dataset1 = new ADataset(PROJECT_ID, "myDataset1");
    datasetRepository.save(dataset1);
    ADataset dataset2 = new ADataset(project, "myDataset2");
    datasetRepository.save(dataset2);
  }

  @Test
  public void simpleTest() {
    ADataset fetchedDataset1 = datasetRepository.findByDatasetId(PROJECT_ID + ":myDataset1").get();
    assertEquals("myDataset1", fetchedDataset1.getDatasetName());
    ADataset fetchedDataset2 = datasetRepository.findByDatasetId(PROJECT_ID + ":myDataset2").get();
    assertEquals("myDataset2", fetchedDataset2.getDatasetName());
  }
}
