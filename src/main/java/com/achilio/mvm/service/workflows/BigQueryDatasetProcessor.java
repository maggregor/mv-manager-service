package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.services.FetcherService;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BigQueryDatasetProcessor implements ItemProcessor<Dataset, ADatasetEntitiesHolder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryJobProcessor.class);
  @Autowired
  private FetcherService fetcherService;

  public ADatasetEntitiesHolder process(@NonNull Dataset dataset) {
    ADataset aDataset = new ADataset(dataset);
    ADatasetEntitiesHolder holder = new ADatasetEntitiesHolder(aDataset);
    fetcherService.fetchAllTables(aDataset.getDatasetId(), aDataset.getDatasetName())
        .filter(this::isValidTable)
        .forEach(table -> {
          ATable aTable = new ATable(aDataset, table.getTableId().getTable());
          List<AColumn> columns = toAColumns(aTable, table);
          holder.addTable(aTable);
          holder.addColumns(columns);
        });
    return holder;
  }

  private List<AColumn> toAColumns(ATable aTable, Table table) {
    TableDefinition definition = table.getDefinition();
    final Schema schema = definition.getSchema();
    if (schema == null) {
      LOGGER.warn("Can't retrieve columns: schema is null");
      return new ArrayList<>();
    }
    return schema.getFields().stream()
        .map(f -> new AColumn(aTable, f.getName(), toZetaSQLStringType(f)))
        .collect(Collectors.toList());
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

  private String toZetaSQLStringType(Field field) {
    final String statusType = field.getType().toString();
    switch (statusType) {
      case "DOUBLE":
      case "FLOAT":
        return ZetaSQLType.TypeKind.TYPE_NUMERIC.name();
      case "INTEGER":
        return ZetaSQLType.TypeKind.TYPE_INT64.name();
      case "BOOLEAN":
        return ZetaSQLType.TypeKind.TYPE_BOOL.name();
      default:
        return "TYPE_" + statusType;
    }
  }

}
