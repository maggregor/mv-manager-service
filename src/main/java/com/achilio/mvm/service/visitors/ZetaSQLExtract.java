package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.ParseResumeLocation;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SqlException;
import com.google.zetasql.resolvedast.ResolvedNodes.ResolvedStatement;
import com.google.zetasql.resolvedast.ResolvedNodes.Visitor;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZetaSQLExtract extends ZetaSQLModelBuilder implements FieldSetExtract {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLExtract.class);
  private static final String BACKTICK = "`";
  private static final AnalyzerOptions options = defaultAnalyzerOptions();

  public ZetaSQLExtract() {
    super();
  }

  public ZetaSQLExtract(Set<ATable> tables) {
    super(tables);
  }

  private static AnalyzerOptions defaultAnalyzerOptions() {
    AnalyzerOptions options = new AnalyzerOptions();
    LanguageOptions languageOptions = options.getLanguageOptions();
    languageOptions.enableMaximumLanguageFeatures();
    languageOptions.setSupportsAllStatementKinds();
    options.setLanguageOptions(languageOptions);
    return options;
  }

  @Override
  public List<ATableId> extractATableIds(Query query) {
    String statement = removeTableNamesBackticks(query.getQuery());
    return extractTableNames(statement).stream()
        .map(
            path -> {
              if (path.isEmpty() || path.size() > 3) {
                return null;
              }
              if (path.size() == 1 && query.hasDefaultDataset()) {
                return ATableId.of(query.getProjectId(), query.getDefaultDataset(), path.get(0));
              } else if (path.size() == 2) {
                return ATableId.of(query.getProjectId(), path.get(0), path.get(1));
              } else if (path.size() == 3) {
                return ATableId.of(path.get(0), path.get(1), path.get(2));
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private List<List<String>> extractTableNames(String statement) {
    return Analyzer.extractTableNamesFromScript(statement, options);
  }

  @Override
  public List<FieldSet> extractAll(Query query) {
    if (query.hasDefaultDataset()) {
      setDefaultDataset(query.getDefaultDataset());
    }
    ZetaSQLFieldSetExtractEntryPointVisitor v =
        new ZetaSQLFieldSetExtractEntryPointVisitor(query.getProjectId(), getRootCatalog());
    resolveStatementAndVisit(query.getQuery(), v);
    return v.getAllFieldSets();
  }

  private void resolveStatementAndVisit(String statement, Visitor visitor) {
    final SimpleCatalog catalog = super.getRootCatalog();
    statement = removeTableNamesBackticks(statement);
    try {
      ParseResumeLocation location = new ParseResumeLocation(statement);
      while (location.getBytePosition() < statement.getBytes().length) {
        ResolvedStatement resolved = Analyzer.analyzeNextStatement(location, options, catalog);
        resolved.accept(visitor);
      }
    } catch (SqlException e) {
      LOGGER.info(
          "Statement analyze has failed because of an SQL error: {} - {}",
          e.getMessage(),
          statement.trim().replaceAll("[\r\n]+", ""));
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
    List<List<String>> paths = extractTableNames(statement);
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
