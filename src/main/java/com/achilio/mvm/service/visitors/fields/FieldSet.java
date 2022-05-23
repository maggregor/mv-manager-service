package com.achilio.mvm.service.visitors.fields;

import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.JoinType;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetIneligibilityReason;
import java.util.Map;
import java.util.Set;

/**
 * Represent set of SQL fields.
 *
 * @see FieldSet
 */
public interface FieldSet {

  /** Returns the number of times the fieldset has been used */
  int getHits();

  /** The fields which this set of fields handle. */
  Set<Field> fields();

  /** Returns the main table */
  ATableId getReferenceTable();

  /** Define the main table of the query. */
  void setReferenceTable(ATableId referenceTable);

  /**
   * Returns the join tables
   *
   * @return
   */
  Map<ATableId, JoinType> getJoinTables();

  /** Add join table with the type of join */
  void addJoinTable(ATableId joinTable, JoinType type);

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
