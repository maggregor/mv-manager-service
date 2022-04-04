package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Query is finalized fetched query, ready to be used by the Extractor */
@Entity
@Getter
@Setter
@Table(
    name = "queries",
    indexes = {
      @Index(name = "job_and_project", columnList = "last_fetcher_query_job_id,project_id"),
      @Index(name = "project", columnList = "project_id")
    })
@EnableJpaAuditing
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Query {

  @ManyToOne private FetcherQueryJob lastFetcherQueryJob;

  @ManyToOne
  @JoinColumn(updatable = false)
  private FetcherQueryJob initialFetcherQueryJob;

  @Column(name = "query_statement", length = 65535)
  private String query;

  // ID of the query is the same as the Google query job
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "project_id")
  private String projectId;

  @Column(name = "use_materialized_view")
  private boolean useMaterializedView;

  @Column(name = "use_cache")
  private boolean useCache;

  @Column(name = "start_time")
  private LocalDate startTime;

  @Column(name = "billed_bytes")
  private long billedBytes = 0;

  @Column(name = "processed_bytes")
  private long processedBytes = 0;

  public Query(
      FetcherQueryJob lastFetcherQueryJob,
      String query,
      String id,
      String projectId,
      boolean useMaterializedView,
      boolean useCache,
      LocalDate startTime,
      QueryUsageStatistics statistics) {
    this.lastFetcherQueryJob = lastFetcherQueryJob;
    this.initialFetcherQueryJob = lastFetcherQueryJob;
    this.query = query;
    this.id = id;
    this.projectId = projectId;
    this.useMaterializedView = useMaterializedView;
    this.useCache = useCache;
    this.startTime = startTime;
    this.billedBytes = statistics.getBilledBytes();
    this.processedBytes = statistics.getProcessedBytes();
  }
}
