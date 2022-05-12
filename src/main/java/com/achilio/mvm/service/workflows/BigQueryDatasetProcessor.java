package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.BigQueryTable;
import com.achilio.mvm.service.services.FetcherService;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BigQueryDatasetProcessor implements ItemProcessor<Dataset, ADataset> {

  private final FetcherService fetcherService;

  public BigQueryDatasetProcessor(FetcherService fetcherService) {
    this.fetcherService = fetcherService;
  }

  /**
   * Converts a BigQuery dataset to ADataset Fetch tables of each dataset and converts to ATable and
   * AColumns.
   *
   * @param dataset
   * @return a ADataset that contains all tables with all columns as Achilio abstractions
   */
  public ADataset process(@NonNull Dataset dataset) {
    ADataset aDataset = new ADataset(dataset);
    fetcherService.fetchAllTables(aDataset.getProjectId(), aDataset.getDatasetName())
        .filter(this::isValidTable)
        .map(BigQueryTable::new)
        .forEach(aDataset::addATable);
    return aDataset;
  }


  /*
   * Filter on:
   * - should exists.
   * - should be a StandardTableDefinition (and not a View or Materialized View).
   */
  private boolean isValidTable(Table table) {
    return table != null
        && table.exists()
        && table.getDefinition() instanceof StandardTableDefinition
        && isEligibleTableDefinition(table.getDefinition());
  }

  /**
   * Returns true if the table is eligible
   *
   * <p>- Don't have RECORD field type
   */
  private boolean isEligibleTableDefinition(StandardTableDefinition tableDefinition) {
    return tableDefinition.getSchema() != null
        && tableDefinition.getSchema().getFields().stream()
        .noneMatch(f -> f.getType().equals(LegacySQLTypeName.RECORD));
  }
}
