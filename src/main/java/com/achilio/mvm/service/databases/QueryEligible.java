package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.visitors.QueryIneligibilityReason;
import java.util.Set;

public interface QueryEligible {

  /**
   * Basically, a query is eligible when no one ineligibility reason has been found.
   *
   * @return
   */
  default boolean isEligible() {
    return getQueryIneligibilityReasons().isEmpty();
  }

  void addQueryIneligibilityReason(QueryIneligibilityReason reason);

  void removeQueryIneligibilityReason(QueryIneligibilityReason reason);
  
  Set<QueryIneligibilityReason> getQueryIneligibilityReasons();
}
