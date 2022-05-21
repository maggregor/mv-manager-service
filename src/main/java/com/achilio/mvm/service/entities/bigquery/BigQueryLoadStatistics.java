package com.achilio.mvm.service.entities.bigquery;

import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;
import lombok.Getter;

@Getter
public class BigQueryLoadStatistics extends BigQueryJobStatistics {

  private final Long badRecords;
  private final Long inputFiles;
  private final Long outputBytes;
  private final Long outputRows;

  public BigQueryLoadStatistics(JobStatistics s) {
    super(s);
    LoadStatistics stats = (LoadStatistics) s;
    this.badRecords = stats.getBadRecords();
    this.inputFiles = stats.getInputFiles();
    this.outputBytes = stats.getOutputBytes();
    this.outputRows = stats.getOutputRows();
  }
}
