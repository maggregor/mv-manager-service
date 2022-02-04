package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import java.util.Set;

/**
 * Represent set of SQL fields.
 *
 * @see FieldSet
 */
public interface FieldSet {

  /**
   * Returns query cost
   */
  long cost();

  /**
   * The fields which this set of fields handle.
   *
   * @return the fields this set of fields handle.
   */
  Set<Field> fields();

  Set<FetchedTable> getReferenceTables();

  void setReferenceTables(Set<FetchedTable> referenceTables);

  QueryUsageStatistics getStatistics();

  void setStatistics(QueryUsageStatistics statistics);

  /**
   * Add new Field to the field set.
   *
   * @return
   */
  void add(Field field);

  /**
   * Merge fields from a given FieldSet
   *
   * @param fieldSet
   */
  void merge(FieldSet fieldSet);

  /**
   * Returns aggregate fields in the field set
   */
  Set<Field> aggregates();

  /**
   * Returns references fields in the field set
   */
  Set<Field> references();

  /**
   * Returns functions fields in the field set
   */
  Set<Field> functions();

  /**
   * Create a FieldSet clone
   */
  FieldSet clone();

  /**
   * Returns true if the fieldset doesn't contains any field.
   *
   * @return
   */
  boolean isEmpty();
}
