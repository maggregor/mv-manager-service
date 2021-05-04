package com.alwaysmart.optimizer.controllers;

import lombok.Data;

import java.util.Date;

@Data
public class OptimizationResponse {

	private String projectId;

	private Date createdAt;

	private int resultCount;

}
