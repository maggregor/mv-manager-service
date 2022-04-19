package com.achilio.mvm.service.workflows;

import com.google.cloud.bigquery.Dataset;
import org.springframework.batch.item.support.IteratorItemReader;

public class DatasetFetcherJobReader extends IteratorItemReader<Dataset> {

  public DatasetFetcherJobReader(Iterable<Dataset> iterable) {
    super(iterable);
  }

  @Override
  public Dataset read() {
    return super.read();
  }
}
