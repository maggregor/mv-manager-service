package com.alwaysmart.optimizer.extract.fields;

import com.alwaysmart.optimizer.OptimizerApplication;
import com.google.cloud.bigquery.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultFieldSet implements FieldSet, Cloneable {

	private static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

	private TableId tableId;
	private Set<Field> fields;
	private long scannedBytesMb;
	private int hits;

	public DefaultFieldSet() {
		this(new LinkedHashSet<>(), 0, 0);
	}

	public DefaultFieldSet(
			final Set<Field> fields,
			final long scannedBytes,
			final int hits) {
		this.fields = fields;
		this.scannedBytesMb = scannedBytes;
		this.hits = hits;
	}

	@Override
	public void setTableId(TableId tableId) {
		this.tableId = tableId;
	}

	@Override
	public TableId getTableId() {
		return this.tableId;
	}

	@Override
	public Set<Field> fields() {
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
	public void add(Field field) {
		this.fields.add(field);
	}

	@Override
	public void merge(FieldSet fieldSet) {
		fields.addAll(fieldSet.fields());
	}

	@Override
	public Set<Field> aggregates() {
		return this.fields.stream().filter(field -> field instanceof AggregateField).collect(Collectors.toSet());
	}

	@Override
	public Set<Field> references() {
		return this.fields.stream().filter(field -> field instanceof ReferenceField).collect(Collectors.toSet());
	}

	@Override
	public FieldSet clone() {
		try {
			return (FieldSet) super.clone();
		} catch (CloneNotSupportedException e) {
			LOGGER.error("Error while FieldSet cloning: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
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
