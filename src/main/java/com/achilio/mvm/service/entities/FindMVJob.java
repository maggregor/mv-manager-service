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

  public enum EventStatus {
    // Retrieve queries from history
    FETCHING_QUERIES,
    // Retrieve tables
    FETCHING_TABLES,
    // Keep the eligible queries
    FILTER_ELIGIBLE_QUERIES,
    // Keep queries from dataset
    FILTER_QUERIES_FROM_DATASET,
    // Extract columns, functions and expression from queries
    EXTRACTING_FIELD_SETS,
    // Merging field sets statistics
    MERGING_FIELD_SETS,
    // Optimizing
    GENERATING_MVs,
    // Materialized view statements building
    BUILD_MATERIALIZED_VIEWS_STATEMENT,
    // Publishing on pub/sub system
    PUBLISHING,
    // Publishing done
    PUBLISHED,
    // Not published
    NOT_PUBLISHED,
  }
}
