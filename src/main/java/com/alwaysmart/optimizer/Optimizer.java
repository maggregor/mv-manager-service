package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;

import java.util.Collection;

/**
 * Optimizer service
 *
 * Able to manipulate field set and returns optimized field set.
 * @see FieldSet
 */
public interface Optimizer {

	/**
	 * Transform raw field set to optimized field set.
	 *
	 * @param fieldSet - Row field set
	 * @return -
	 */
	Collection<FieldSet> optimize(Collection<FieldSet> fieldSet);
}
