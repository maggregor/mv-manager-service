package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.ATable;
import java.util.Set;

public class FieldSetExtractFactory {

  public static FieldSetExtract createFieldSetExtract(Set<ATable> tables) {
    return new ZetaSQLExtract(tables);
  }
}
