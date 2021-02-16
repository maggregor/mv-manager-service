package com.alwaysmart.optimizer.fields;

import java.util.Set;

public class FieldSet {

	private Set<Field> fields;
	private long scanned;
	private long hits;

	public FieldSet(Set<Field> fields) {
		this.fields = fields;
	}

	public Set<Field> getFields() {
		return fields;
	}

	public void setFields(Set<Field> fields) {
		this.fields = fields;
	}

	public long getScanned() {
		return scanned;
	}

	public void setScanned(long scanned) {
		this.scanned = scanned;
	}

	public long getHits() {
		return hits;
	}

	public void setHits(long hits) {
		this.hits = hits;
	}
}
