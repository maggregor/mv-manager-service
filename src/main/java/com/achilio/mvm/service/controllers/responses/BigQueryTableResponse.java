package com.achilio.mvm.service.controllers.responses;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.BigQueryTable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BigQueryTableResponse extends ATableResponse {

  @JsonProperty("numBytes")
  private final long numBytes;

  @JsonProperty("numLongTermBytes")
  private final long numLongTermBytes;

  public BigQueryTableResponse(ATable aTable) {
    super(aTable);
    BigQueryTable table = (BigQueryTable) aTable;
    this.numBytes = table.getNumBytes();
    this.numLongTermBytes = table.getNumLongTermBytes();
  }
}
