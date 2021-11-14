package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FieldSetHelper {
  ;

  static FieldSet statementToFieldSet(String statement, FieldSetAnalyzer extractor) {
    FetchedQuery query = FetchedQueryFactory.createFetchedQuery(statement);
    return extractor.extract(query);
  }

  static FieldSet createFieldSet(Field... fields) {
    return createFieldSet(new HashSet<>(Arrays.asList(fields)));
  }

  static FieldSet createFieldSet(Set<Field> fields) {
    return new DefaultFieldSet(fields);
  }
}
