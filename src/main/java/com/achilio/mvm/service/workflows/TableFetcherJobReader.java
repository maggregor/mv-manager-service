package com.achilio.mvm.service.workflows;

import com.google.cloud.bigquery.Table;
import org.springframework.batch.item.support.IteratorItemReader;

public class TableFetcherJobReader extends IteratorItemReader<Table> {

  public TableFetcherJobReader(Iterable<Table> iterable) {
    super(iterable);
  }

  @Override
  public Table read() {
    return super.read();
  }
}
