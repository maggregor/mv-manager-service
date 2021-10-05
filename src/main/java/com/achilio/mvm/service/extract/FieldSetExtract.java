package com.achilio.mvm.service.extract;

import com.achilio.mvm.service.extract.fields.FieldSet;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FieldSetBuilder create field set ready to be optimized.
 *
 * @see FieldSet
 */
public interface FieldSetExtract {

	/**
	 * Extract all fields from each sql statements and returns each FieldSet.
	 * Discover tables paths in the statements
	 *
	 * @param fetchedQueries - the queries
	 * @return
	 */
	default Set<FieldSet> extract(List<FetchedQuery> fetchedQueries) {
		Set<FieldSet> fieldSets = new HashSet<>();
		for (FetchedQuery query : fetchedQueries) {
			if(discoverTablePath(query)) {
				FieldSet fieldSet = extract(query);
				fieldSets.add(fieldSet);
			}
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

	/**
	 * Extract all schemas / tables id
	 *
	 * @param fetchedQueries - the query
	 * @return true if a path was found
	 */
	boolean
	discoverTablePath(FetchedQuery fetchedQueries);

	/**
	 * Register data model in the extractor.
	 *
	 * @param tables
	 */
	void registerTables(List<FetchedTable> tables);

	void registerTable(FetchedTable table);

	boolean isTableRegistered(final String dataset, final String tableName);
}
