package com.achilio.mvm.service.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "projects_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class ProjectMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="project_id", nullable = false)
	private String projectId;

	@Column(name="activated", nullable = false)
	private Boolean activated;

	public ProjectMetadata() {	}

	public ProjectMetadata(String projectId, Boolean activated) {
		this.projectId = projectId;
		this.activated = activated;
	}

	public Long getId() {
		return id;
	}

	public String getProjectId() {
		return projectId;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}
}
