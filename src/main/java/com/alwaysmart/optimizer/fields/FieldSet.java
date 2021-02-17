package com.alwaysmart.optimizer.fields;

import com.google.common.annotations.VisibleForTesting;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class FieldSet {

	private Set<Field> fields = new LinkedHashSet<>();
	private long scanned;
	private long hits;

	public FieldSet() {

	}

	@VisibleForTesting
	public FieldSet(Field field) {
		this.fields.add(field);
	}

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

	public void addField(Field field) {
		this.fields.add(field);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldSet fieldSet = (FieldSet) o;
		return scanned == fieldSet.scanned &&
				hits == fieldSet.hits &&
				Objects.equals(fields, fieldSet.fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fields, scanned, hits);
	}

	@Override
	public String toString() {
		return "FieldSet{" +
				"fields=" + fields +
				", scanned=" + scanned +
				", hits=" + hits +
				'}';
	}
}
