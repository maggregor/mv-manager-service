package com.achilio.mvm.service.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;

@Entity
@Table(name = "tables_metadata")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class TableMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private ProjectMetadata projectMetadata;

	@ManyToOne
	private DatasetMetadata datasetMetadata;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;
}
