package com.achilio.mvm.service.entities.bigquery;

import com.google.cloud.bigquery.JobStatistics;
import lombok.Getter;

@Getter
public class BigQueryExtractStatistics extends BigQueryJobStatistics {
  
  public BigQueryExtractStatistics(JobStatistics s) {
    super(s);
  }
}
