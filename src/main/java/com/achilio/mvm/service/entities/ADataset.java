package com.achilio.mvm.service.entities;

import com.google.cloud.bigquery.Dataset;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<ATable> tables = new ArrayList<>();

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

  public void addATable(ATable table) {
    this.tables.add(table);
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
