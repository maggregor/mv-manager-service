package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;

import java.util.Collection;

public class BruteForceOptimizer implements Optimizer {

	@Override
	public Collection<FieldSet> optimize(Collection<FieldSet> fieldSet) {
		return fieldSet;
	}
}
