package com.achilio.mvm.service.workflows;

import com.google.cloud.bigquery.Job;
import org.springframework.batch.item.support.IteratorItemReader;

public class FetcherQueryReader extends IteratorItemReader<Job> {

  public FetcherQueryReader(Iterable<Job> iterable) {
    super(iterable);
  }

  @Override
  public Job read() {
    return super.read();
  }
}
