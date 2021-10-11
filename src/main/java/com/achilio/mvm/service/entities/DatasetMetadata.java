package com.achilio.mvm.service.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;

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

	@Column(name = "enabled", nullable = false)
	private boolean enabled;
}
