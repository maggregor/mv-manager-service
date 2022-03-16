package com.achilio.mvm.service.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "fetcher_struct_jobs")
public class FetcherStructJob extends FetcherJob {

  public FetcherStructJob() {}

  public FetcherStructJob(String projectId) {
    super(projectId);
  }
}
