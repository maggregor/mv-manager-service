package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class FindMVJob extends Job {

  @Column(name = "timeframe")
  private int timeframe;

  @Column(name = "mv_proposal_count")
  private Integer mvProposalCount;

  public FindMVJob(String projectId, int timeframe) {
    super(projectId);
    this.timeframe = timeframe;
  }
}
