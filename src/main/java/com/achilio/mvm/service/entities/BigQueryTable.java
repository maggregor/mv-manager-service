package com.achilio.mvm.service.entities;

import com.google.api.client.util.Preconditions;
import com.google.cloud.bigquery.ExternalTableDefinition;
import com.google.cloud.bigquery.MaterializedViewDefinition;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.ViewDefinition;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Getter
@NoArgsConstructor
@DiscriminatorValue("bigquery")
public class BigQueryTable extends ATable {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryTable.class);

  @Column(name = "num_bytes")
  private Long numBytes = 0L;

  @Column(name = "num_long_term_bytes")
  private Long numLongTermBytes = 0L;

  public BigQueryTable(Table table) {
    Preconditions.checkNotNull(table, "Table is null");
    TableId tableId = table.getTableId();
    this.setProjectId(tableId.getProject());
    this.setDatasetName(table.getTableId().getDataset());
    this.setTableName(table.getTableId().getTable());
    super.setTableId();
    this.numBytes = table.getNumBytes();
    this.numLongTermBytes = table.getNumLongTermBytes();
    TableDefinition definition = table.getDefinition();
    this.setType(getTableType(definition));
    Schema schema = definition.getSchema();
    if (schema == null) {
      throw new IllegalArgumentException("Schema is null");
    }
    this.setColumns(schema.getFields().stream()
        .map(f -> {
          try {
            return (AColumn) new BigQueryColumn(this.getProjectId(), this.getTableId(), f);
          } catch (Exception e) {
            LOGGER.warn("Ignored field: {} cause: {}", f.getName(), e.getMessage());
          }
          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
  }

  /**
   * https://cloud.google.com/bigquery/pricing#storage
   * <p> numBytes gives the total storage footprint</p>
   * <p>LongTermBytes gives the number of bytes that have a cheaper pricing</p>
   * <p>Standard storage pricing: $0.02 / Gb</p>
   * <p>Long term storage pricing: $0.01 / Gb</p>
   * <p>$0.02 * numBytes - numLongTermBytes / (1024^3) - $0.01 * numLongTermBytes / (1024^3)</p>
   */
  @Override
  public Float getCost() {
    final int gb = 1024 * 1024 * 1024;
    return (float) ((numBytes - numLongTermBytes) / gb * 0.02 + numLongTermBytes / gb * 0.01);
  }

  private TableType getTableType(TableDefinition definition) {
    if (definition instanceof StandardTableDefinition) {
      return TableType.TABLE;
    } else if (definition instanceof MaterializedViewDefinition) {
      return TableType.MATERIALIZED_VIEW;
    } else if (definition instanceof ViewDefinition) {
      return TableType.VIEW;
    } else if (definition instanceof ExternalTableDefinition) {
      return TableType.EXTERNAL_TABLE;
    } else {
      return TableType.UNKNOWN;
    }
  }
}
