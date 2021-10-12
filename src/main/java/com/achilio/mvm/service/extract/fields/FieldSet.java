package com.achilio.mvm.service.extract.fields;

import java.util.Set;

/**
 * Represent set of SQL fields.
 *
 * @see FieldSet
 */
public interface FieldSet {

  String getProjectId();

  void setProjectId(String projectName);

  String getDataset();

  void setDataset(String datasetName);

  String getTable();

  void setTable(String tableName);

  /**
   * The fields which this set of fields handle.
   *
   * @return the fields this set of fields handle.
   */
  Set<Field> fields();

  /**
   * The potential total of scanned bytes coverable by this set of field.
   *
   * @return the potential total of scanned bytes coverable by this set of field.
   */
  long scannedBytesMb();

  /**
   * The potential number of usage this set of field has received.
   *
   * @return the potential number of usage this set of field has received.
   */
  int hits();

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

  /** Returns aggregate fields in the field set */
  Set<Field> aggregates();

  /** Returns references fields in the field set */
  Set<Field> references();

  /** Create a FieldSet clone */
  FieldSet clone();
}
