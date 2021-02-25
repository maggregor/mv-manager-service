package com.alwaysmart.optimizer.fields;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DefaultFieldSet implements FieldSet {

	private List<Field> fields;
	private long scannedBytesMb;
	private int hits;

	public DefaultFieldSet() {
		this(new LinkedList<>(), 0, 0);
	}

	public DefaultFieldSet(
			final List<Field> fields,
			final long scannedBytes,
			final int hits) {
		this.fields = fields;
		this.scannedBytesMb = scannedBytes;
		this.hits = hits;
	}

	@Override
	public List<Field> fields() {
		return fields;
	}

	@Override
	public long scannedBytesMb() {
		return scannedBytesMb;
	}

	@Override
	public int hits() {
		return hits;
	}

	@Override
	public Field fieldAt(int index) {
		return this.fields.get(index);
	}

	@Override
	public void add(Field field) {
		if (!this.fields.contains(field)) {
			this.fields.add(field);
		}
	}

	@Override
	public void merge(FieldSet fieldSet) {
		fields.addAll(fieldSet.fields());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultFieldSet that = (DefaultFieldSet) o;
		return scannedBytesMb == that.scannedBytesMb &&
				hits == that.hits &&
				Objects.equals(fields, that.fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fields, scannedBytesMb, hits);
	}

	@Override
	public String toString() {
		return "DefaultFieldSet{" +
				"fields=" + fields +
				", scannedBytesMb=" + scannedBytesMb +
				", hits=" + hits +
				'}';
	}
}
