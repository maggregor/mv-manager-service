package com.alwaysmart.optimizer.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class 	DatasetMetadataResponse {

	@JsonProperty("activated")
	private final boolean activated;

	public DatasetMetadataResponse(boolean activated) {
		this.activated = activated;
	}

	public boolean isActivated() {
		return activated;
	}

}
