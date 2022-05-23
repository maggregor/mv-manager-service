package com.achilio.mvm.service;

import static com.achilio.mvm.service.FieldSetHelper.createFieldSet;

import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.fields.AggregateField;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FunctionField;
import com.achilio.mvm.service.visitors.fields.ReferenceField;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetIneligibilityReason;
import java.util.Arrays;

public class FieldSetBuilder {

  private FieldSet fieldSet;

  public FieldSetBuilder() {
    fieldSet = createFieldSet();
  }

  public FieldSetBuilder(ATableId tableId) {
    fieldSet = createFieldSet(tableId);
  }

  public static FieldSetBuilder fsBuilder() {
    return new FieldSetBuilder();
  }

  FieldSetBuilder addRef(String name) {
    this.fieldSet.add(new ReferenceField(name));
    return this;
  }

  FieldSetBuilder addAgg(String name) {
    this.fieldSet.add(new AggregateField(name));
    return this;
  }

  FieldSetBuilder addFunc(String name) {
    this.fieldSet.add(new FunctionField(name));
    return this;
  }

  FieldSetBuilder addIneligibility(FieldSetIneligibilityReason reason) {
    this.fieldSet.addIneligibilityReason(reason);
    return this;
  }

  FieldSetBuilder setRefTable(ATableId tableId) {
    this.fieldSet.setReferenceTable(tableId);
    return this;
  }

  FieldSet build() {
    return fieldSet;
  }

  public FieldSetBuilder addJoinTable(ATableId tableId, JoinType joinType) {
    this.fieldSet.addJoinTable(tableId, joinType);
    return this;
  }

  public FieldSetBuilder addRefs(String... fields) {
    Arrays.stream(fields).forEach(this::addRef);
    return this;
  }
}
