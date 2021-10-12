package com.achilio.mvm.service.extract.fields;

import com.achilio.mvm.service.OptimizerApplication;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFieldSet implements FieldSet, Cloneable {

  private static Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  private String projectId;
  private String dataset;
  private String table;
  private Set<Field> fields;
  private long scannedBytesMb;
  private int hits;

  public DefaultFieldSet() {
    this(new LinkedHashSet<>(), 0, 0);
  }

  public DefaultFieldSet(final Set<Field> fields, final long scannedBytes, final int hits) {
    this.fields = fields;
    this.scannedBytesMb = scannedBytes;
    this.hits = hits;
  }

  @Override
  public String getProjectId() {
    return this.projectId;
  }

  @Override
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public String getDataset() {
    return this.dataset;
  }

  @Override
  public void setDataset(String datasetName) {
    this.dataset = datasetName;
  }

  @Override
  public String getTable() {
    return this.table;
  }

  @Override
  public void setTable(String tableName) {
    this.table = tableName;
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
    return this.fields.stream()
        .filter(field -> field instanceof AggregateField)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Field> functions() {
    return this.fields.stream()
        .filter(field -> field instanceof FunctionField)
        .filter(field -> !aggregates().contains(field))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Field> references() {
    return this.fields.stream()
        .filter(field -> field instanceof ReferenceField)
        .collect(Collectors.toSet());
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
    return scannedBytesMb == that.scannedBytesMb
        && hits == that.hits
        && Objects.equals(dataset, that.dataset)
        && Objects.equals(table, that.table)
        && Objects.equals(fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataset, table, fields, scannedBytesMb, hits);
  }

  @Override
  public String toString() {
    return "DefaultFieldSet{"
        + "dataset='"
        + dataset
        + '\''
        + ", table='"
        + table
        + '\''
        + ", fields="
        + fields
        + ", scannedBytesMb="
        + scannedBytesMb
        + ", hits="
        + hits
        + '}';
  }
}
