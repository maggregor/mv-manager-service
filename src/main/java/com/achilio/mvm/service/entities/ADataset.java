package com.achilio.mvm.service.entities;

import com.google.cloud.bigquery.Dataset;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "datasets")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class ADataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(ADataset.class);

  //  @Id
  //  @GeneratedValue(strategy = GenerationType.AUTO)
  //  private Long id;

  @Id
  @Column(unique = true)
  private String datasetId;

  @JoinColumn(
      name = "project_id",
      referencedColumnName = "projectId",
      insertable = false,
      updatable = false)
  @ManyToOne(targetEntity = Project.class, fetch = FetchType.LAZY)
  private Project project;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "activated", nullable = false)
  private boolean activated = true;

  public ADataset(Project project, String datasetName) {
    this.project = project;
    this.projectId = project.getProjectId();
    this.datasetName = datasetName;
    setDatasetId(project.getProjectId(), datasetName);
  }

  public ADataset(Dataset dataset) {
    this.projectId = dataset.getDatasetId().getProject();
    this.datasetName = dataset.getDatasetId().getDataset();
    setDatasetId(this.projectId, this.datasetName);
  }

  public ADataset(String projectId, String datasetName) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    setDatasetId(projectId, datasetName);
  }

  public void setDatasetId(String projectId, String datasetName) {
    this.datasetId = String.format("%s:%s", projectId, datasetName);
  }

  public void setActivated(Boolean activated) {
    if (activated != null) {
      this.activated = activated;
      LOGGER.info("Update dataset {} activated={}", datasetId, activated);
    }
  }

  @Override
  public int hashCode() {
    return datasetId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ADataset aDataset = (ADataset) o;

    return datasetId.equals(aDataset.datasetId);
  }
}
