package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Query is finalized fetched query, ready to be used by the Extractor */
@Entity
@Table(name = "queries", indexes = @Index(columnList = "fetcher_query_job_id"))
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Query {

  @ManyToOne private FetcherQueryJob fetcherQueryJob;

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
      FetcherQueryJob fetcherQueryJob,
      String query,
      String id,
      String projectId,
      boolean useMaterializedView,
      boolean useCache,
      LocalDate startTime,
      QueryUsageStatistics statistics) {
    this.fetcherQueryJob = fetcherQueryJob;
    this.query = query;
    this.id = id;
    this.projectId = projectId;
    this.useMaterializedView = useMaterializedView;
    this.useCache = useCache;
    this.startTime = startTime;
    this.billedBytes = statistics.getBilledBytes();
    this.processedBytes = statistics.getProcessedBytes();
  }

  public Query() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getQuery() {
    return query;
  }

  public boolean isUseMaterializedView() {
    return useMaterializedView;
  }

  public boolean isUseCache() {
    return useCache;
  }

  public LocalDate getStartTime() {
    return startTime;
  }

  public long getBilledBytes() {
    return billedBytes;
  }

  public long getProcessedBytes() {
    return processedBytes;
  }

  public FetcherQueryJob getFetcherQueryJob() {
    return fetcherQueryJob;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }
}
