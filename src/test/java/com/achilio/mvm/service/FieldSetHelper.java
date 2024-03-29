package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetExtract;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum FieldSetHelper {
  ;

  static List<FieldSet> statementToFieldSet(
      String projectId, String statement, FieldSetExtract extractor) {
    AQuery query = new AQuery();
    query.setProjectId(projectId);
    query.setQuery(statement);
    return extractor.extractAll(query);
  }

  static FieldSet createFieldSet() {
    return createFieldSet(new HashSet<>());
  }

  static FieldSet createFieldSet(ATableId tableId) {
    return createFieldSet(tableId, new HashSet<>());
  }

  static FieldSet createFieldSet(Field... fields) {
    return createFieldSet(new HashSet<>(Arrays.asList(fields)));
  }

  static FieldSet createFieldSet(Set<Field> fields) {
    return new DefaultFieldSet(fields);
  }

  static FieldSet createFieldSet(ATableId referenceTable, Field... fields) {
    return createFieldSet(referenceTable, new HashSet<>(Arrays.asList(fields)));
  }

  static FieldSet createFieldSet(ATableId referenceTable, Set<Field> fields) {
    FieldSet fieldSet = new DefaultFieldSet(fields);
    fieldSet.setReferenceTable(referenceTable);
    return fieldSet;
  }
}
