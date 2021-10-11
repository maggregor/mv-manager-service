package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.fields.FieldSet;

import java.util.Set;

/**
 * Optimizer service
 * <p>
 * Able to manipulate field set and returns optimized field set.
 *
 * @see FieldSet
 */
public interface Optimizer {

	/**
	 * Transform raw field set to optimized field set.
	 *
	 * @param fieldSet - Row field set
	 * @return -
	 */
	Set<FieldSet> optimize(Set<FieldSet> fieldSet);
}
