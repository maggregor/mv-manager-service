package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.OptimizerApplication;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.FieldSetIneligibilityReason;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.TableId;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFieldSet implements FieldSet {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptimizerApplication.class);

  private final Set<Field> fields;
  private final Map<TableId, JoinType> joinTables = new HashMap<>();
  private TableId referenceTable;
  private QueryUsageStatistics statistics;

  public DefaultFieldSet() {
    this(new LinkedHashSet<>());
  }

  public DefaultFieldSet(final Set<Field> fields) {
    this.fields = fields;
  }

  @Override
  public Set<Field> fields() {
    return fields;
  }

  @Override
  public TableId getReferenceTable() {
    return this.referenceTable;
  }

  @Override
  public void setReferenceTable(TableId referenceTable) {
    this.referenceTable = referenceTable;
  }

  @Override
  public Map<TableId, JoinType> getJoinTables() {
    return joinTables;
  }

  @Override
  public void addJoinTable(TableId joinTable, JoinType type) {
    this.joinTables.put(joinTable, type);
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
  public long cost() {
    return this.statistics == null ? 0 : this.statistics.getProcessedBytes();
  }

  @Override
  public boolean isEmpty() {
    return fields.isEmpty() || this == FieldSetFactory.EMPTY_FIELD_SET;
  }

  @Override
  public void addIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {}

  @Override
  public void removeIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {}

  @Override
  public void clearIneligibilityReasons(FieldSetIneligibilityReason ineligibilityReason) {}

  @Override
  public Set<FieldSetIneligibilityReason> getIneligibilityReasons() {
    return null;
  }

  @Override
  public boolean isEligible() {
    return false;
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
  public String toString() {
    return "DefaultFieldSet{"
        + "fields="
        + fields
        + ", referenceTable="
        + referenceTable
        + ", joinTables="
        + joinTables
        + '}';
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(fields()).toHashCode();
  }
}
