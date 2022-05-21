package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.bigquery.BigQueryTable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BigQueryTableResponse extends ATableResponse {

  @JsonProperty("numBytes")
  private final long numBytes;

  public BigQueryTableResponse(BigQueryTable aTable) {
    super(aTable);
    this.numBytes = aTable.getNumBytes();
  }
}
