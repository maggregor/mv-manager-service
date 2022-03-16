package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class FetcherQueryJob extends FetcherJob {

  /** Timeframe is the number of days before today the fetching query job starts from */
  @Column private int timeframe;

  protected FetcherQueryJob() {}

  public FetcherQueryJob(String projectId) {
    this(projectId, 7);
  }

  public FetcherQueryJob(String projectId, int timeframe) {
    super(projectId);
    this.timeframe = timeframe;
  }

  public int getTimeframe() {
    return timeframe;
  }
}
