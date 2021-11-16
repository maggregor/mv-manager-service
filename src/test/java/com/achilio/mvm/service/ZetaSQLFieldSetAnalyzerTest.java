package com.achilio.mvm.service;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.FieldSetAnalyzer;
import com.achilio.mvm.service.visitors.FieldSetExtractVisitor;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import com.achilio.mvm.service.visitors.ZetaSQLFieldSetExtractVisitor;
import java.util.Set;
import org.junit.Test;

public class ZetaSQLFieldSetAnalyzerTest extends FieldSetExtractTest {

  @Override
  protected FieldSetAnalyzer createFieldSetExtract(String projectName, Set<FetchedTable> tables) {
    return new ZetaSQLExtract(projectName, tables);
  }

  @Test
  public void addField() {
    FieldSetExtractVisitor extract = new ZetaSQLFieldSetExtractVisitor();
  }
}
