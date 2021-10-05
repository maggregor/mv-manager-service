package com.achilio.mvm.service.databases;

public interface IQueryElector {

    boolean isEligibleInnerJoin();

    boolean isEligibleCrossJoin();

    boolean isEligibleLeftJoin();

    boolean isEligibleRightJoin();

}
