package com.achilio.mvm.service.entities.bigquery;

import com.google.cloud.bigquery.JobStatistics;
import lombok.Getter;

@Getter
public class BigQueryCopyStatistics extends BigQueryJobStatistics {


  public BigQueryCopyStatistics(JobStatistics s) {
    super(s);
  }
}
