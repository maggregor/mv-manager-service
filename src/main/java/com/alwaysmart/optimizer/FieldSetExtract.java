package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;

import java.util.Collection;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface FieldSetExtract {

	/**
	 * Extract all fields from sql statement and returns FieldSet.
	 *
	 * @param fetchedQueries - all queries
	 * @return
	 */
	Collection<FieldSet> extract(Collection<FetchedQuery> fetchedQueries);
}
