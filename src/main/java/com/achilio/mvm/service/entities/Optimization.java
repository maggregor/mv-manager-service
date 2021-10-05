package com.achilio.mvm.service.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "optimizations")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Optimization {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@CreatedDate
	@Column(name = "created_date", nullable = false)
	private Date createdDate;

	@Column(name = "project_id", nullable = false)
	private String projectId;

	@Column(name = "region_id", nullable = false)
	private String regionId;

	@Column(name = "dataset_name", nullable = false)
	private String datasetName;

	@Column(name = "approval_required", nullable = false)
	private boolean approvalRequired;

	public Optimization(String projectId, String regionId, String datasetName, boolean approvalRequired) {
		this.projectId = projectId;
		this.regionId = regionId;
		this.datasetName = datasetName;
		this.approvalRequired = approvalRequired;
	}

	public Optimization() {}

	public Long getId() {
		return id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getRegionId() {
		return projectId;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public boolean isApprovalRequired() {
		return approvalRequired;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Optimization that = (Optimization) o;
		return createdDate == that.createdDate &&
				approvalRequired == that.approvalRequired &&
				id.equals(that.id) &&
				projectId.equals(that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, createdDate, projectId, approvalRequired);
	}
}
