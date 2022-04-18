package com.achilio.mvm.service.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Query is finalized fetched query, ready to be used by the Extractor */
@Entity
@Getter
@Setter
@Table(
    name = "queries",
    indexes = {@Index(name = "project", columnList = "project_id")})
@EnableJpaAuditing
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition = "varchar(31) default 'BigQueryJob'")
public class Query {

  @Column(name = "query_statement", columnDefinition = "text")
  private String query;

  // ID of the query is the same as the Google query job
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "project_id")
  private String projectId;

  @Column(name = "use_materialized_view")
  private boolean useMaterializedView = false;

  @Column(name = "use_cache")
  private boolean useCache = false;

  @Column(name = "start_time")
  private Date startTime;

  @Column(name = "billed_bytes")
  private long billedBytes = 0;

  @Column(name = "processed_bytes")
  private long processedBytes = 0;

  @Column(name = "default_dataset")
  private String defaultDataset = null;

  @Column(name = "error", columnDefinition = "text")
  private String error = null;

  @Column(name = "tables")
  @ElementCollection
  // TODO an index ? -> Maybe: @CollectionTable(indexes = {@Index(columnList = "tables")})
  private List<String> tableId = new ArrayList<>();

  public Query(String query, String projectId) {
    this.query = query;
    this.projectId = projectId;
  }

  public Query(
      String query,
      String id,
      String projectId,
      String defaultDataset,
      boolean useMaterializedView,
      boolean useCache,
      Date startTime) {
    this.query = query;
    this.id = id;
    this.projectId = projectId;
    this.defaultDataset = defaultDataset;
    this.useMaterializedView = useMaterializedView;
    this.useCache = useCache;
    this.startTime = startTime;
  }

  public boolean hasDefaultDataset() {
    return StringUtils.isNotEmpty(defaultDataset);
  }
}
