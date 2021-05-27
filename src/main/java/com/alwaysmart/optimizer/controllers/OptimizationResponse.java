package com.alwaysmart.optimizer.controllers;

import lombok.Data;

@Data
public class OptimizationResponse {

	private Long optimizationId;

	private String projectId;

	public OptimizationResponse(Long optimizationId, String projectId) {
		this.optimizationId = optimizationId;
		this.projectId = projectId;
	}
}
