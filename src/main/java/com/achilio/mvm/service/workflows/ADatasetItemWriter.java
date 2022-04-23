package com.achilio.mvm.service.workflows;


import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.repositories.ADatasetRepository;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ADatasetItemWriter implements ItemWriter<ADataset> {

  private final ADatasetRepository datasetRepository;

  public ADatasetItemWriter(ADatasetRepository datasetRepository) {
    this.datasetRepository = datasetRepository;
  }

  @Override
  public void write(List<? extends ADataset> datasets) {
    for (ADataset dataset : datasets) {
      datasetRepository.deleteByDatasetId(dataset.getDatasetId());
      datasetRepository.save(dataset);
    }
  }
}
