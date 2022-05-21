package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.entities.TableRef;
import com.achilio.mvm.service.entities.TableRef.TableRefType;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.QueryPatternExtract;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum QueryPatternHelper {
  ;

  static List<QueryPattern> statementToQueryPattern(
      String projectId, String statement, QueryPatternExtract extractor) {
    AQuery query = new AQuery();
    query.setProjectId(projectId);
    query.setQuery(statement);
    return extractor.extractAll(query);
  }

  static QueryPattern createQueryPattern() {
    return createQueryPattern(new HashSet<>());
  }

  static QueryPattern createQueryPattern(ATableId tableId) {
    return createQueryPattern(tableId, new HashSet<>());
  }

  static QueryPattern createQueryPattern(Field... fields) {
    return createQueryPattern(new HashSet<>(Arrays.asList(fields)));
  }

  static QueryPattern createQueryPattern(Set<Field> fields) {
    QueryPattern queryPattern = new QueryPattern();
    fields.forEach(queryPattern::add);
    return queryPattern;
  }

  static QueryPattern createQueryPattern(ATableId referenceTable, Field... fields) {
    return createQueryPattern(referenceTable, new HashSet<>(Arrays.asList(fields)));
  }

  static QueryPattern createQueryPattern(ATableId referenceTable, Set<Field> fields) {
    QueryPattern queryPattern = new QueryPattern();
    fields.forEach(queryPattern::add);
    queryPattern.addTableRef(new TableRef(referenceTable, TableRefType.MAIN));
    return queryPattern;
  }
}
