package com.achilio.mvm.service.visitors.querypattern;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetExtract;
import java.util.Set;

public class QueryPatternExtractFactory {

  public static FieldSetExtract createFieldSetExtract(Set<ATable> tables) {
    return new ZetaSQLExtract(tables);
  }
}
