package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.MVGeneratorApplication;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.FieldSetIneligibilityReason;
import com.achilio.mvm.service.visitors.JoinType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFieldSet implements FieldSet {

  private static final Logger LOGGER = LoggerFactory.getLogger(MVGeneratorApplication.class);

  private final Set<Field> fields = new LinkedHashSet<>();
  private final Map<ATableId, JoinType> joinTables = new HashMap<>();
  private final Set<FieldSetIneligibilityReason> ineligibilityReasons = new HashSet<>();
  private ATableId referenceTable;
  private int hits = 0;

  public DefaultFieldSet() {}

  public DefaultFieldSet(final Set<Field> fields) {
    addAll(fields);
  }

  private void addAll(Set<Field> fields) {
    fields.forEach(this::add);
  }

  @Override
  public int hashCode() {
    int result = fields.hashCode();
    result = 31 * result + joinTables.hashCode();
    result = 31 * result + ineligibilityReasons.hashCode();
    result = 31 * result + (referenceTable != null ? referenceTable.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultFieldSet)) {
      return false;
    }

    DefaultFieldSet fieldSet = (DefaultFieldSet) o;

    if (!Objects.equals(fields, fieldSet.fields)) {
      return false;
    }
    if (!joinTables.equals(fieldSet.joinTables)) {
      return false;
    }
    if (!ineligibilityReasons.equals(fieldSet.ineligibilityReasons)) {
      return false;
    }
    return Objects.equals(referenceTable, fieldSet.referenceTable);
  }

  @Override
  public String toString() {
    return "DefaultFieldSet{"
        + "fields="
        + fields
        + ", joinTables="
        + joinTables
        + ", ineligibilityReasons="
        + ineligibilityReasons
        + ", referenceTable="
        + referenceTable
        + '}';
  }

  @Override
  public int getHits() {
    return this.hits;
  }

  @Override
  public Set<Field> fields() {
    return fields;
  }

  @Override
  public ATableId getReferenceTable() {
    return this.referenceTable;
  }

  @Override
  public void setReferenceTable(ATableId referenceTable) {
    this.referenceTable = referenceTable;
  }

  @Override
  public Map<ATableId, JoinType> getJoinTables() {
    return joinTables;
  }

  @Override
  public void addJoinTable(ATableId joinTable, JoinType type) {
    this.joinTables.put(joinTable, type);
  }

  @Override
  public void add(Field field) {
    this.hits++;
    this.fields.add(field);
  }

  @Override
  public void merge(FieldSet fieldSet) {
    addAll(fieldSet.fields());
  }

  @Override
  public Set<Field> aggregates() {
    return this.fields.stream()
        .filter(field -> field instanceof AggregateField)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Field> references() {
    return this.fields.stream()
        .filter(field -> field instanceof ReferenceField)
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
  public boolean isEmpty() {
    return fields.isEmpty();
  }

  @Override
  public void addIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {
    ineligibilityReasons.add(ineligibilityReason);
  }

  @Override
  public void removeIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {
    ineligibilityReasons.remove(ineligibilityReason);
  }

  @Override
  public void clearIneligibilityReasons() {
    ineligibilityReasons.clear();
  }

  @Override
  public Set<FieldSetIneligibilityReason> getIneligibilityReasons() {
    return Collections.unmodifiableSet(ineligibilityReasons);
  }

  @Override
  public boolean isEligible() {
    return ineligibilityReasons.isEmpty();
  }
}
