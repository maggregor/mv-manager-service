package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import java.time.LocalDate;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** AchilioQuery is finalized fetched query, ready to be used by the Extractor */
@Entity
@Table(name = "achilio_query")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class AchilioQuery {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "fetcher_job")
  @ManyToOne
  private FetcherQueryJob fetcherQueryJob;

  @Column(name = "reasons")
  @ElementCollection
  private Set<QueryIneligibilityReason> reasons;

  @Column(name = "query_statement")
  private String query;

  @Column(name = "use_materialized_view")
  private boolean useMaterializedView;

  @Column(name = "useCache")
  private boolean useCache;

  @Column(name = "start_time")
  private LocalDate startTime;

  // Discovered tables in the SQL query
  // TODO: ManyToOne reference to fetched tables when AchilioFetchedTable is persisted
  @Column(name = "ref_tables")
  @ElementCollection
  private Set<String> refTables;

  @Column(name = "query_count")
  private int queryCount = 0;

  @Column(name = "billed_bytes")
  private long billedBytes = 0;

  @Column(name = "processed_bytes")
  private long processedBytes = 0;

  public AchilioQuery(
      FetcherQueryJob fetcherQueryJob,
      String query,
      boolean useMaterializedView,
      boolean useCache,
      LocalDate startTime,
      Set<String> refTables,
      QueryUsageStatistics statistics) {
    this.fetcherQueryJob = fetcherQueryJob;
    this.query = query;
    this.useMaterializedView = useMaterializedView;
    this.useCache = useCache;
    this.startTime = startTime;
    this.refTables = refTables;
    this.queryCount = statistics.getQueryCount();
    this.billedBytes = statistics.getBilledBytes();
    this.processedBytes = statistics.getProcessedBytes();
  }

  protected AchilioQuery() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public Set<String> getRefTables() {
    return refTables;
  }

  public int getQueryCount() {
    return queryCount;
  }

  public long getBilledBytes() {
    return billedBytes;
  }

  public long getProcessedBytes() {
    return processedBytes;
  }
}
