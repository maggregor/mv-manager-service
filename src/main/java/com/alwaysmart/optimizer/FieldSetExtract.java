package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;

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
	List<FieldSet> extract(List<FetchedQuery> fetchedQueries, TableMetadata metadata);



	/**
	 * Extract all fields from the sql statement and returns FieldSet.
	 *
	 * @param fetchedQueries - the query
	 * @return
	 */
	FieldSet extract(FetchedQuery fetchedQueries, TableMetadata metadata);
}
