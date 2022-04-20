package com.achilio.mvm.service.workflows;


import com.achilio.mvm.service.repositories.AColumnRepository;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import com.achilio.mvm.service.repositories.ATableRepository;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ADatasetEntitiesHolderItemWriter implements
    ItemWriter<ADatasetEntitiesHolder> {

  @Autowired
  private ADatasetRepository datasetRepository;
  @Autowired
  private ATableRepository tableRepository;
  @Autowired
  private AColumnRepository columnRepository;

  @Override
  public void write(List<? extends ADatasetEntitiesHolder> items) {
    for (ADatasetEntitiesHolder item : items) {
      datasetRepository.delete(item.getDataset());
      datasetRepository.save(item.getDataset());
      tableRepository.saveAll(item.getTables());
      columnRepository.saveAll(item.getColumns());
    }
  }
}
