package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.ZetaSQLOptions.LanguageFeature;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;
import com.google.zetasql.resolvedast.ResolvedNodes.Visitor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    languageOptions.enableLanguageFeature(LanguageFeature.FEATURE_V_1_3_ALLOW_DASHES_IN_TABLE_NAME);
    options.setLanguageOptions(new LanguageOptions().enableMaximumLanguageFeatures());
    options.setPruneUnusedColumns(true);
    return options;
  }

  @Override
  @Deprecated
  public FieldSet extract(FetchedQuery fetchedQuery) {
    final String projectId = getProjectId();
    final SimpleCatalog catalog = getCatalog();
    FieldSetExtractVisitor v = new ZetaSQLFieldSetExtractStatementVisitor(projectId, catalog);
    resolveAndVisit(fetchedQuery, v);
    return v.getFieldSet();
  }

  @Override
  public List<FieldSet> extractAll(FetchedQuery fetchedQuery) {
    return extractAll(fetchedQuery.getProjectId(), fetchedQuery.getQuery());
  }

  @Override
  public List<FieldSet> extractAll(String projectId, String statement) {
    ZetaSQLFieldSetExtractEntryPointVisitor v =
        new ZetaSQLFieldSetExtractEntryPointVisitor(projectId, getCatalog());
    resolveStatementAndVisit(statement, v);
    return v.getAllFieldSets();
  }

  @Override
  public List<FieldSet> extractAll(List<FetchedQuery> fetchedQueries) {
    return fetchedQueries.stream()
        .map(this::extractAll)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private void resolveAndVisit(FetchedQuery fetchedQuery, Visitor visitor) {
    resolveStatementAndVisit(fetchedQuery.getQuery(), visitor);
  }

  private void resolveStatementAndVisit(String statement, Visitor visitor) {
    final SimpleCatalog catalog = super.getCatalog();
    try {
      statement = removeTableNamesBackticks(statement);
      ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
      resolvedStatement.accept(visitor);
    } catch (Exception e) {
      LOGGER.error("Query resolving has failed: {}", e.getMessage());
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
      TableId tableId = TableId.parse(tablePath);
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
