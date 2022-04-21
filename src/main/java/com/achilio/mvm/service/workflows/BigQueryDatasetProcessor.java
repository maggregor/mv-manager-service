package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.services.FetcherService;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.zetasql.ZetaSQLType.TypeKind;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BigQueryDatasetProcessor implements ItemProcessor<Dataset, ADataset> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryJobProcessor.class);

  private final FetcherService fetcherService;

  public BigQueryDatasetProcessor(FetcherService fetcherService) {
    this.fetcherService = fetcherService;
  }

  public ADataset process(@NonNull Dataset dataset) {
    ADataset aDataset = new ADataset(dataset);
    final String projectId = aDataset.getProjectId();
    final String datasetName = aDataset.getDatasetName();
    fetcherService.fetchAllTables(aDataset.getProjectId(), aDataset.getDatasetName())
        .filter(this::isValidTable)
        .map(table -> toATable(projectId, datasetName, table))
        .forEach(aDataset::addATable);
    return aDataset;
  }

  private ATable toATable(String projectId, String datasetName, Table table) {
    String tableName = table.getTableId().getTable();
    ATable aTable = new ATable(projectId, datasetName, tableName);
    List<AColumn> columns = toAColumns(aTable, table);
    aTable.setColumns(columns);
    return aTable;
  }

  private List<AColumn> toAColumns(ATable aTable, Table table) {
    TableDefinition definition = table.getDefinition();
    final Schema schema = definition.getSchema();
    if (schema == null) {
      LOGGER.warn("Can't retrieve columns: schema is null");
      return new ArrayList<>();
    }
    return schema.getFields().stream()
        .map(f -> new AColumn(aTable.getProjectId(), aTable.getTableId(), f.getName(),
            toZetaSQLType(f).toString()))
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

  /**
   * Columns type mapping. Between BigQuery enum TypeKind and ZetaSQL type.
   *
   * @param field
   * @return
   */
  private TypeKind toZetaSQLType(Field field) {
    final StandardSQLTypeName statusType = field.getType().getStandardType();
    switch (statusType) {
      case FLOAT64:
        return TypeKind.TYPE_FLOAT;
      case NUMERIC:
        return TypeKind.TYPE_NUMERIC;
      case BOOL:
        return TypeKind.TYPE_BOOL;
      case DATE:
        return TypeKind.TYPE_DATE;
      case TIME:
        return TypeKind.TYPE_TIME;
      case TIMESTAMP:
        return TypeKind.TYPE_TIMESTAMP;
      case BYTES:
        return TypeKind.TYPE_BYTES;
      case ARRAY:
        return TypeKind.TYPE_ARRAY;
      case INT64:
        return TypeKind.TYPE_UINT64;
      case DATETIME:
        return TypeKind.TYPE_DATETIME;
      case STRUCT:
        return TypeKind.TYPE_STRUCT;
      case GEOGRAPHY:
        return TypeKind.TYPE_GEOGRAPHY;
      case BIGNUMERIC:
        return TypeKind.TYPE_BIGNUMERIC;
      case STRING:
        return TypeKind.TYPE_STRING;
    }
    throw new IllegalArgumentException("Unsupported column type  " + statusType);
  }

}
