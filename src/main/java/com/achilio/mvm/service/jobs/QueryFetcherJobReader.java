package com.achilio.mvm.service.jobs;

import com.google.cloud.bigquery.Job;
import org.springframework.batch.item.support.IteratorItemReader;

public class QueryFetcherJobReader extends IteratorItemReader<Job> {

  public QueryFetcherJobReader(Iterable<Job> iterable) {
    super(iterable);
  }

  @Override
  public Job read() {
    return super.read();
  }
}
