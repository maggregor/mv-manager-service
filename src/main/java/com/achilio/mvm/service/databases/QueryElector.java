package com.achilio.mvm.service.databases;

public class QueryElector implements IQueryElector {

	public boolean isValid(final String statement) {
		return true;
	}

	@Override
	public boolean isEligibleInnerJoin() {
		return false;
	}

	@Override
	public boolean isEligibleCrossJoin() {
		return false;
	}

	@Override
	public boolean isEligibleLeftJoin() {
		return false;
	}

	@Override
	public boolean isEligibleRightJoin() {
		return false;
	}
}
