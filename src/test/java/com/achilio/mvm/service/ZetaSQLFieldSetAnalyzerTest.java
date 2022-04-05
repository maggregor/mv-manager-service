package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.visitors.FieldSetExtract;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.Set;

public class ZetaSQLFieldSetAnalyzerTest extends FieldSetExtractTest {

  @Override
  protected FieldSetExtract createFieldSetExtract(String projectName, Set<ATable> tables) {
    return new ZetaSQLExtract(projectName, tables);
  }
}
