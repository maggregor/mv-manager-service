package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("bigquery")
public class BigQueryTable extends ATable {

  @Column(name = "num_bytes")
  private Long numBytes = 0L;

  @Column(name = "num_long_term_bytes")
  private Long numLongTermBytes = 0L;

  public BigQueryTable(String projectId, String datasetName, String tableName) {
    super(projectId, datasetName, tableName);
  }

  /**
   * https://cloud.google.com/bigquery/pricing#storage
   * <p>$0.02 * num bytes / (1024^3) + $0.02 * num long term bytes / (1024^3)</p>
   */
  @Override
  public Double getCost() {
    return (this.numBytes / (1024 * 1024 * 1024) * 0.02
        + this.numLongTermBytes / (1024 * 1024
        * 1024) * 0.01);
  }
}
