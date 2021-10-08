package com.achilio.mvm.service;

import com.achilio.mvm.service.extract.ZetaSQLFieldSetExtractVisitor;
import com.achilio.mvm.service.extract.fields.FieldSet;
import com.achilio.mvm.service.extract.fields.FieldSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public class BruteForceOptimizer implements Optimizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BruteForceOptimizer.class);

	private int maxFieldSet;

	public BruteForceOptimizer(int maxFieldSet) {
		this.maxFieldSet = maxFieldSet;
	}
	@Override
	public Set<FieldSet> optimize(Set<FieldSet> fieldSet) {
		fieldSet.remove(FieldSetFactory.EMPTY_FIELD_SET);
		removeExcessFieldSet(fieldSet);
		return fieldSet;
	}

	private void removeExcessFieldSet(Set<FieldSet> fieldSets) {
		Iterator<FieldSet> iterator = fieldSets.iterator();
		int i = 1;
		while(iterator.hasNext()) {
			FieldSet current = iterator.next();
			if (i > maxFieldSet) {
				iterator.remove();
				LOGGER.info("Fieldset limit reached. Removed " + current.toString());
			}
			i++;
		}
	}
}
