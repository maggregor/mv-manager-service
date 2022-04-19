package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.ADataset;
import com.google.cloud.bigquery.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class BigQueryDatasetProcessor implements ItemProcessor<Dataset, ADataset> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryJobProcessor.class);

  @Override
  public ADataset process(@NonNull Dataset dataset) {
    ADataset aDataset = new ADataset(dataset);
    return aDataset;
  }
}
