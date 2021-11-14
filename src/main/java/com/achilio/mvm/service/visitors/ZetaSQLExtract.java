package com.achilio.mvm.service.visitors;

import static com.google.zetasql.Analyzer.extractTableNamesFromStatement;

import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.ZetaSQLOptions.LanguageFeature;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZetaSQLExtract extends ZetaSQLAnalyzedContext implements FieldSetAnalyzer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLExtract.class);
  private final AnalyzerOptions options = defaultAnalyzerOptions();

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
  public void analyzeIneligibleReasons(FetchedQuery fetchedQuery) {
    final String statement = fetchedQuery.getQuery();
    final SimpleCatalog catalog = this.getCatalog();
    discoverFetchedTable(fetchedQuery);
    try {
      ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
      resolvedStatement.accept(new ZetaSQLFetchedQueryElectorVisitor(fetchedQuery));
    } catch (Exception e) {
      LOGGER.error("Query resolving has failed", e);
      fetchedQuery.addQueryIneligibilityReason(QueryIneligibilityReason.PARSING_FAILED);
    }
  }

  @Override
  public FieldSet extract(FetchedQuery fetchedQuery) {
    final String statement = fetchedQuery.getQuery();
    final SimpleCatalog catalog = this.getCatalog();
    try {
      discoverFetchedTable(fetchedQuery);
      ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
      FieldSetExtractVisitor extractVisitor = new ZetaSQLFieldSetExtractGlobalVisitor(catalog);
      resolvedStatement.accept(extractVisitor);
      FieldSet fieldSet = extractVisitor.fieldSet();
      fieldSet.setReferenceTables(fetchedQuery.getReferenceTables());
      fieldSet.setStatistics(fetchedQuery.getStatistics());
      return fieldSet;
    } catch (Exception e) {
      LOGGER.error("Query resolving has failed", e);
      fetchedQuery.addQueryIneligibilityReason(QueryIneligibilityReason.PARSING_FAILED);
      return FieldSetFactory.EMPTY_FIELD_SET;
    }
  }

  @Override
  public void discoverFetchedTable(FetchedQuery fetchedQuery) {
    List<List<String>> allPaths = findTablePaths(fetchedQuery);
    Set<FetchedTable> tables = allPaths.stream()
        .map(this::findFetchedTableByPath)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    fetchedQuery.setReferenceTables(tables);
    if (tables.isEmpty()) {
      LOGGER.debug("No one table path found for this query: " + fetchedQuery.getQuery());
    }
  }

  private FetchedTable findFetchedTableByPath(List<String> path) {
    Optional<FetchedTable> optionalFetchedTable = Optional.empty();
    if (path.size() == 1) {
      // Have to split
      List<String> splitPath = Arrays.asList(path.get(0).split("\\."));
      if (splitPath.size() >= 2) {
        return findFetchedTableByPath(splitPath);
      }
    } else if (path.size() == 2) {
      optionalFetchedTable = getFetchedTable(path.get(0), path.get(1));
    } else if (path.size() == 3) {
      optionalFetchedTable = getFetchedTable(path.get(1), path.get(2));
    }
    if (optionalFetchedTable.isPresent()) {
      return optionalFetchedTable.get();
    }
    LOGGER.warn("Can't find a fetched table with this table path: {}", path);
    return null;
  }

  private Optional<FetchedTable> getFetchedTable(String datasetName, String tableName) {
    return tables().stream()
        .filter(t -> t.getDatasetName().equalsIgnoreCase(datasetName))
        .filter(t -> t.getTableName().equalsIgnoreCase(tableName))
        .findFirst();
  }

  private List<List<String>> findTablePaths(FetchedQuery fetchedQuery) {
    final String statement = fetchedQuery.getQuery();
    try {
      return extractTableNamesFromStatement(statement, options);
    } catch (Exception e) {
      LOGGER.error("Can't find table paths from query: {}", statement, e);
      return Collections.emptyList();
    }
  }

}
