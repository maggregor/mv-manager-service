package com.alwaysmart.optimizer.fields;

import java.util.List;

/**
 * Represent set of SQL fields.
 * @see FieldSet
 */
public interface FieldSet {

	/**
	 * The fields which this set of fields handle.
	 *
	 * @return the fields this set of fields handle.
	 */
	List<Field> fields();

	/**
	 * The potential total of scanned bytes coverable by this set of field.
	 *
	 * @return the potential total of scanned bytes coverable by this set of
	 * field.
	 */
	long scannedBytesMb();


	/**
	 * The potential number of usage this set of field has received.
	 *
	 * @return the potential number of usage this set of field has received.
	 */
	int hits();

	/**
	 * The field at given index in the field set.
	 *
	 * @return the field at given index in the field set.
	 */
	Field fieldAt(int index);

	/**
	 * Add new Field to the field set.
	 *
	 * @return
	 */
	void add(Field field);
}
