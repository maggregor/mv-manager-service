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
import java.util.Objects;

@Entity
@Table(name = "results")
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class OptimizationResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Optimization optimization;

	@Column(name="dataset_name", nullable = false)
	private String dataset;

	@Column(name="table_name", nullable = false)
	private String table;

	@Column(name="statement", nullable = false, length = 65536)
	private String statement;

	public OptimizationResult(String dataset, String table, Optimization optimization, String statement) {
		this.dataset = dataset;
		this.table = table;
		this.optimization = optimization;
		this.statement = statement;
	}

	public OptimizationResult() {

	}

	public Long getId() {
		return id;
	}

	public String getStatement() {
		return statement;
	}

	public Optimization getOptimization() {
		return optimization;
	}

	public String getDataset() {
		return dataset;
	}

	public String getTable() {
		return table;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OptimizationResult that = (OptimizationResult) o;
		return id.equals(that.id) &&
				optimization.equals(that.optimization) &&
				dataset.equals(that.dataset) &&
				table.equals(that.table) &&
				statement.equals(that.statement);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, optimization, dataset, table, statement);
	}
}
