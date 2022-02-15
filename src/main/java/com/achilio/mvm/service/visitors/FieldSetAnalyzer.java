package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface FieldSetAnalyzer {

  /**
   * Extract all fields from each sql statements and returns each FieldSet. Discover tables paths in
   * the statements
   *
   * @param fetchedQueries - the queries
   * @return
   */
  default List<FieldSet> extract(List<FetchedQuery> fetchedQueries) {
    return fetchedQueries.stream().map(this::extract).collect(Collectors.toList());
  }

  /**
   * Extract all fields from the sql statement and returns FieldSet.
   *
   * @param fetchedQueries - the query
   * @return
   */
  FieldSet extract(FetchedQuery fetchedQueries);

  /**
   * Find table path and map with FetchedTable
   *
   * @param fetchedQueries - the query
   * @return
   */
  void discoverFetchedTable(FetchedQuery fetchedQueries);

  default void analyzeIneligibleReasons(List<FetchedQuery> fetchedQueries) {
    fetchedQueries.forEach(this::analyzeIneligibleReasons);
  }

  void analyzeIneligibleReasons(FetchedQuery fetchedQuery);
}
