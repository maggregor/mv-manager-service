package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;

import java.util.LinkedList;
import java.util.List;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface FieldSetExtract {

	/**
	 * Extract all fields from each sql statements and returns each FieldSet.
	 *
	 * @param fetchedQueries - the queries
	 * @return
	 */
	default List<FieldSet> extract(List<FetchedQuery> fetchedQueries) {
		List<FieldSet> fieldSets = new LinkedList<>();
		for (FetchedQuery query : fetchedQueries) {
			fieldSets.add(extract(query));
		}
		return fieldSets;
	}

	/**
	 * Extract all fields from the sql statement and returns FieldSet.
	 *
	 * @param fetchedQueries - the query
	 * @return
	 */
	FieldSet extract(FetchedQuery fetchedQueries);
}
