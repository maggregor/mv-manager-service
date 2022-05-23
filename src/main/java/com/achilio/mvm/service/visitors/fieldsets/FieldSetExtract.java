package com.achilio.mvm.service.visitors.fieldsets;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.visitors.ATableId;
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

  List<ATableId> extractATableIds(AQuery query);

  List<FieldSet> extractAll(AQuery query);

  default List<FieldSet> extractAll(List<AQuery> queries) {
    return queries.stream()
        .map(this::extractAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
