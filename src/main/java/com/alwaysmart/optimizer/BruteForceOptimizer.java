package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.alwaysmart.optimizer.extract.fields.FieldSetFactory;

import java.util.Set;

public class BruteForceOptimizer implements Optimizer {

	@Override
	public Set<FieldSet> optimize(Set<FieldSet> fieldSet) {
		fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
		return fieldSet;
	}
}
