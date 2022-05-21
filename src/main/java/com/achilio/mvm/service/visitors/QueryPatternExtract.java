package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface QueryPatternExtract {

  List<ATableId> extractATableIds(AQuery query);

  List<QueryPattern> extractAll(AQuery query);

  default List<QueryPattern> extractAll(List<AQuery> queries) {
    return queries.stream()
        .map(this::extractAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
