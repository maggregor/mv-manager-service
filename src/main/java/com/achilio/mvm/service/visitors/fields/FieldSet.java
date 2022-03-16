package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.visitors.FieldSetIneligibilityReason;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.TableId;
import java.util.Map;
import java.util.Set;

/**
 * Represent set of SQL fields.
 *
 * @see FieldSet
 */
public interface FieldSet {

  /** Returns query cost */
  long cost();

  /** The fields which this set of fields handle. */
  Set<Field> fields();

  /**
   * Returns the main table
   *
   * @return
   */
  TableId getReferenceTable();

  /** Define the main table of the query. */
  void setReferenceTable(TableId referenceTable);

  /**
   * Returns the join tables
   *
   * @return
   */
  Map<TableId, JoinType> getJoinTables();

  /** Add join table with the type of join */
  void addJoinTable(TableId joinTable, JoinType type);

  /** Returns the statistics of the FieldSet */
  QueryUsageStatistics getStatistics();

  void setStatistics(QueryUsageStatistics statistics);

  /** Add new Field to the field set. */
  void add(Field field);

  /** Merge fields from a given FieldSet */
  void merge(FieldSet fieldSet);

  /** Returns aggregate fields in the field set */
  Set<Field> aggregates();

  /** Returns references fields in the field set */
  Set<Field> references();

  /** Returns functions fields in the field set */
  Set<Field> functions();

  /** Returns true if the fieldset doesn't contain any field. */
  boolean isEmpty();

  /** Add ineligibility reason */
  void addIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason);

  /** Remove ineligibility reason */
  void removeIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason);

  /** Clear ineligibility reasons */
  void clearIneligibilityReasons();

  /** Returns ineligibility reasons */
  Set<FieldSetIneligibilityReason> getIneligibilityReasons();

  /** Returns true if the fieldset is eligible */
  boolean isEligible();
}
