package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.OptimizerApplication;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFieldSet implements FieldSet, Cloneable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  private Set<Field> fields = new HashSet<>();
  private Set<FetchedTable> referenceTables;
  private QueryUsageStatistics statistics;
  private int totalHits = 0;

  public DefaultFieldSet() {
    this(new HashSet<>());
  }

  public DefaultFieldSet(final Set<Field> fields) {
    fields.forEach(this::add);
    this.statistics = new QueryUsageStatistics(0, 0, 0);
  }

  @Override
  public Set<Field> fields() {
    return fields;
  }

  @Override
  public Set<FetchedTable> getReferenceTables() {
    return this.referenceTables;
  }

  @Override
  public void setReferenceTables(Set<FetchedTable> referenceTables) {
    this.referenceTables = referenceTables;
  }

  @Override
  public QueryUsageStatistics getStatistics() {
    return this.statistics;
  }

  @Override
  public void setStatistics(QueryUsageStatistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public void add(Field field) {
    this.totalHits++;
    this.fields.add(field);
  }

  @Override
  public void merge(FieldSet fieldSet) {
    this.fields.addAll(fieldSet.fields());
    this.totalHits += fieldSet.getTotalHits();
    this.getStatistics().addQueryUsageStatistics(fieldSet.getStatistics());
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
  public long cost() {
    return this.statistics == null ? 0 : this.statistics.getProcessedBytes();
  }

  @Override
  public FieldSet clone() {
    try {
      return (FieldSet) super.clone();
    } catch (CloneNotSupportedException e) {
      LOGGER.error("Error while FieldSet cloning: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public boolean isEmpty() {
    return fields.isEmpty();
  }

  @Override
  public int getTotalHits() {
    return this.totalHits;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultFieldSet that = (DefaultFieldSet) o;
    return new EqualsBuilder().append(fields, that.fields).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(fields()).toHashCode();
  }

  @Override
  public String toString() {
    return "DefaultFieldSet{" + "fields=" + fields + ", totalHits=" + totalHits + '}';
  }
}
