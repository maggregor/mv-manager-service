package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import java.util.Collections;
import java.util.Set;

public class FieldSetExtractFactory {

  public static FieldSetAnalyzer createFieldSetExtract(String projectId) {
    return createFieldSetExtract(projectId, Collections.emptySet());
  }

  public static FieldSetAnalyzer createFieldSetExtract(String projectId, Set<FetchedTable> tables) {
    return new ZetaSQLExtract(projectId, tables);
  }
}
