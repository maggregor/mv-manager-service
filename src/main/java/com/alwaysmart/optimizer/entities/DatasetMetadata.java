package com.alwaysmart.optimizer.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "datasets_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class DatasetMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private ProjectMetadata projectMetadata;

	@Column(name="enabled", nullable = false)
	private boolean enabled;
}
