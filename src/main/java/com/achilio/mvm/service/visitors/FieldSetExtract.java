package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface FieldSetExtract {

  List<FieldSet> extractAll(Query query);

  default List<FieldSet> extractAll(List<Query> queries) {
    return queries.stream()
        .map(this::extractAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
