package com.alwaysmart.optimizer.extract;

import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.cloud.bigquery.TableId;

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
	 *
	 * @param fetchedQueries - the queries
	 * @return
	 */
	default Set<FieldSet> extract(List<FetchedQuery> fetchedQueries) {
		Set<FieldSet> fieldSets = new HashSet<>();
		for (FetchedQuery query : fetchedQueries) {
			FieldSet fieldSet = extract(query);
			fieldSets.add(fieldSet);
			// Hack - Keep the TableId in the fieldset. Dirty here.
			fieldSet.setTableId(query.getTableId());
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
	 * @return
	 */
	default Set<TableId> extractAllTableId(List<FetchedQuery> fetchedQueries) {
		Set<TableId> fieldSets = new HashSet<>();
		fetchedQueries.forEach(fetchedQuery -> fieldSets.addAll(extractTableId(fetchedQuery)));
		return fieldSets;
	}

	/**
	 * Extract all schemas / tables id
	 *
	 * @param fetchedQueries - the query
	 * @return
	 */
	Set<TableId> extractTableId(FetchedQuery fetchedQueries);

	/**
	 * Register data model in the extractor.
	 *
	 * @param tables
	 */
	void registerTables(List<FetchedTable> tables);
}
