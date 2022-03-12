package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class FetcherQueryJob extends FetcherJob {

  private static Logger LOGGER = LoggerFactory.getLogger(FetcherQueryJob.class);

  @Column(name = "timeframe", nullable = false)
  private Long timeframe;

  protected FetcherQueryJob() {}

  public FetcherQueryJob(String projectId) {
    this(projectId, 7L);
  }

  public FetcherQueryJob(String projectId, Long timeframe) {
    super(projectId);
    this.timeframe = timeframe;
  }

  public Long getTimeframe() {
    return timeframe;
  }
}
