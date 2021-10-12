package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedQueryFactory;
import com.achilio.mvm.service.extract.FieldSetExtract;
import com.achilio.mvm.service.extract.fields.DefaultFieldSet;
import com.achilio.mvm.service.extract.fields.Field;
import com.achilio.mvm.service.extract.fields.FieldSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FieldSetHelper {
  ;

  public static FieldSet statementToFieldSet(String statement, FieldSetExtract extractor) {
    FetchedQuery query = FetchedQueryFactory.createFetchedQuery(statement);
    return extractor.extract(query);
  }

  public static FieldSet createFieldSet(Field... fields) {
    return createFieldSet(0, 0, new HashSet<>(Arrays.asList(fields)));
  }

  public static FieldSet createFieldSet(long scannedBytesMb, int hits, Field... fields) {
    return createFieldSet(scannedBytesMb, hits, new HashSet<>(Arrays.asList(fields)));
  }

  public static FieldSet createFieldSet(long scannedBytesMb, int hits, Set<Field> fields) {
    return new DefaultFieldSet(fields, scannedBytesMb, hits);
  }
}
