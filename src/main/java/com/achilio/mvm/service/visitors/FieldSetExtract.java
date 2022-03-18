package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
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

  List<FieldSet> extractAll(FetchedQuery fetchedQuery);

  default List<FieldSet> extractAll(List<FetchedQuery> fetchedQueries) {
    return fetchedQueries.stream()
        .map(this::extractAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
