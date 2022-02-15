package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import java.util.Set;

public class ZetaSQLFieldSetAnalyzerTest extends FieldSetAnalyzerTest {

  @Override
  protected FieldSetAnalyzer createFieldSetExtract(String projectName, Set<FetchedTable> tables) {
    return new ZetaSQLExtract(projectName, tables);
  }
}
