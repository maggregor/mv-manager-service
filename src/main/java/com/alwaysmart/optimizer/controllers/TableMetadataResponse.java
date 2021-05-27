package com.alwaysmart.optimizer.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableMetadataResponse {

	@JsonProperty("activated")
	private final boolean activated;

	public TableMetadataResponse(boolean activated) {
		this.activated = activated;
	}

	public boolean isActivated() {
		return activated;
	}

}
