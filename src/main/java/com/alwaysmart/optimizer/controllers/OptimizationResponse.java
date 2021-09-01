package com.alwaysmart.optimizer.controllers;

import com.alwaysmart.optimizer.entities.Optimization;
import lombok.Data;

@Data
public class OptimizationResponse {

	private Long id;
	private String projectId;
	private String datasetName;
	private String regionId;

	public OptimizationResponse(Optimization optimization) {
		this.id = optimization.getId();
		this.projectId = optimization.getProjectId();
		this.datasetName = optimization.getDatasetName();
		this.regionId = optimization.getRegionId();
	}
}
