package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.ParseResumeLocation;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;
import com.google.zetasql.resolvedast.ResolvedNodes.Visitor;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZetaSQLExtract extends ZetaSQLModelBuilder implements FieldSetExtract {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLExtract.class);
  private static final String BACKTICK = "`";
  private final AnalyzerOptions options = defaultAnalyzerOptions();

  public ZetaSQLExtract(String projectName) {
    super(projectName, Collections.emptySet());
  }

  public ZetaSQLExtract(String projectName, Set<FetchedTable> tables) {
    super(projectName, tables);
  }

  private AnalyzerOptions defaultAnalyzerOptions() {
    AnalyzerOptions options = new AnalyzerOptions();
    LanguageOptions languageOptions = options.getLanguageOptions();
    languageOptions.enableMaximumLanguageFeatures();
    languageOptions.setSupportsAllStatementKinds();
    options.setLanguageOptions(languageOptions);
    return options;
  }

  @Override
  public List<FieldSet> extractAll(FetchedQuery fetchedQuery) {
    if (fetchedQuery.hasDefaultDataset()) {
      setDefaultDataset(fetchedQuery.getDefaultDataset());
    }
    ZetaSQLFieldSetExtractEntryPointVisitor v =
        new ZetaSQLFieldSetExtractEntryPointVisitor(fetchedQuery.getProjectId(), getCatalog());
    resolveStatementAndVisit(fetchedQuery.getQuery(), v);
    if (!v.getAllFieldSets().isEmpty()) {
      fetchedQuery.setCanUseMaterializedViews(true);
    }
    return v.getAllFieldSets();
  }

  private void resolveStatementAndVisit(String statement, Visitor visitor) {
    final SimpleCatalog catalog = super.getCatalog();
    statement = removeTableNamesBackticks(statement);
    try {
      ParseResumeLocation location = new ParseResumeLocation(statement);
      while (location.getBytePosition() < statement.getBytes().length) {
        ResolvedStatement resolved = Analyzer.analyzeNextStatement(location, options, catalog);
        resolved.accept(visitor);
      }
    } catch (Exception e) {
      LOGGER.error(
          "Statement analyze has failed: {} - {}",
          e.getMessage(),
          statement.trim().replaceAll("[\r\n]+", ""));
    }
  }

  private String removeTableNamesBackticks(String statement) {
    if (!StringUtils.containsAny(statement, BACKTICK)) {
      return statement;
    }
    List<List<String>> paths = Analyzer.extractTableNamesFromScript(statement, options);
    for (List<String> path : paths) {
      if (path.size() != 1) {
        // Not a full BackTicked path
        continue;
      }
      String tablePath = path.get(0);
      ATableId tableId = ATableId.parse(tablePath);
      if (tableId == null) {
        // Not a TableId pattern.
        continue;
      }
      String tablePathWithoutBackTicks = tableId.asPath();
      String tablePathFullyBackTicked = String.format("`%s`", tableId.asPath());
      statement = statement.replaceAll(tablePathFullyBackTicked, tablePathWithoutBackTicks);
    }
    return statement;
  }
}
