package com.achilio.mvm.service.entities;

import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jboss.logging.Logger;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("bigquery")
public class BigQueryTable extends ATable {

  private static final Logger LOGGER = Logger.getLogger(BigQueryTable.class);

  @Column(name = "num_bytes")
  private Long numBytes = 0L;

  @Column(name = "num_long_term_bytes")
  private Long numLongTermBytes = 0L;

  public BigQueryTable(Table table) {
    TableId tableId = table.getTableId();
    this.setProjectId(tableId.getProject());
    this.setDatasetName(table.getTableId().getDataset());
    this.setTableName(table.getTableId().getTable());
    this.numBytes = table.getNumBytes();
    this.numLongTermBytes = table.getNumLongTermBytes();
    TableDefinition definition = table.getDefinition();
    Schema schema = definition.getSchema();
    if (schema == null) {
      LOGGER.warn("Can't retrieve columns: schema is null");
    } else {
      this.setColumns(schema.getFields().stream()
          .map(f -> new BigQueryColumn(this.getProjectId(), this.getTableId(), f))
          .collect(Collectors.toList()));
    }
    super.setTableId();
  }

  /**
   * https://cloud.google.com/bigquery/pricing#storage
   * <p>$0.02 * num bytes / (1024^3) + $0.02 * num long term bytes / (1024^3)</p>
   */
  @Override
  public Float getCost() {
    final int gb = 1024 * 1024 * 1024;
    return (float) ((numBytes - numLongTermBytes) / gb * 0.02
        + numLongTermBytes / gb * 0.01);
  }
}
