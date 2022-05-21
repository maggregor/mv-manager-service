package com.achilio.mvm.service.entities.bigquery;

import com.achilio.mvm.service.entities.AQueryStatistics;
import com.google.cloud.bigquery.JobStatistics;
import java.util.Date;
import lombok.Setter;

@Setter
public abstract class BigQueryJobStatistics extends AQueryStatistics {

  private Date createdAt;

  public BigQueryJobStatistics(JobStatistics stats) {
    super.setStartAt(stats.getStartTime() == null ? null : new Date(stats.getStartTime()));
    super.setEndAt(stats.getEndTime() == null ? null : new Date(stats.getEndTime()));
    this.setCreatedAt(stats.getCreationTime() == null ? null : new Date(stats.getCreationTime()));
  }

}
