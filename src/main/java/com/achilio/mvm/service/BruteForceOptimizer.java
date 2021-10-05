package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.fields.FieldSet;
import com.achilio.mvm.service.extract.fields.FieldSetFactory;

import java.util.Set;

public class BruteForceOptimizer implements Optimizer {

	@Override
	public Set<FieldSet> optimize(Set<FieldSet> fieldSet) {
		fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
		return fieldSet;
	}
}
