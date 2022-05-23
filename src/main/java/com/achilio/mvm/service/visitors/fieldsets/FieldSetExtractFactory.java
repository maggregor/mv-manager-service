package com.achilio.mvm.service.visitors.fieldsets;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.Set;

public class FieldSetExtractFactory {

  public static FieldSetExtract createFieldSetExtract(Set<ATable> tables) {
    return new ZetaSQLExtract(tables);
  }
}
