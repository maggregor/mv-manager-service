package com.alwaysmart.optimizer;

import com.google.cloud.solutions.datalineage.extractor.ColumnLineageExtractor;
import com.google.cloud.solutions.datalineage.extractor.ColumnLineageExtractorFactory;
import com.google.cloud.solutions.datalineage.extractor.FunctionExpressionsExtractor;
import com.google.cloud.solutions.datalineage.extractor.GroupByExtractor;
import com.google.cloud.solutions.datalineage.extractor.SimpleAggregateExtractor;
import com.google.cloud.solutions.datalineage.model.LineageMessages;
import com.google.cloud.solutions.datalineage.model.QueryColumns;
import com.google.cloud.solutions.datalineage.service.ZetaSqlSchemaLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.resolvedast.ResolvedNodes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.zetasql.Analyzer.extractTableNamesFromStatement;

public class BigQueryParser {

	static {
		ColumnLineageExtractorFactory.register(
				FunctionExpressionsExtractor.class, GroupByExtractor.class,
				SimpleAggregateExtractor.class);
	}

	private final ZetaSqlSchemaLoader tableSchemaLoader;

	public BigQueryParser(@Nullable ZetaSqlSchemaLoader tableSchemaLoader) {
		this.tableSchemaLoader = tableSchemaLoader;
	}

	public ImmutableSet<LineageMessages.ColumnLineage> extractColumnLineage(String sql) {

		if (tableSchemaLoader == null) {
			return ImmutableSet.of();
		}

		ColumnLineageExtractorFactory columnLineageExtractorFactory =
				ColumnLineageExtractorFactory.forStatement(resolve(sql));

		return combine(columnLineageExtractorFactory.outputColumns(),
				extractLineages(columnLineageExtractorFactory.buildExtractors()));
	}

	/**
	 * Combines output query Column names with their lineage information.
	 *
	 * @param queryColumns the output query columns
	 * @param lineageParts lineage information for complex operation output columns.
	 * @return a combined lookup of column names and lineage component.
	 */
	private ImmutableSet<LineageMessages.ColumnLineage> combine(
			QueryColumns queryColumns,
			ImmutableSet<ImmutableMap<LineageMessages.ColumnEntity, LineageMessages.ColumnLineage>> lineageParts) {

		return queryColumns.getColumnMap().entrySet().stream()
				.map(
						entry -> {
							LineageMessages.ColumnEntity oColumn = LineageMessages.ColumnEntity.newBuilder().setColumn(entry.getKey()).build();

							for (ImmutableMap<LineageMessages.ColumnEntity, LineageMessages.ColumnLineage> lineageMap : lineageParts) {
								if (lineageMap.containsKey(entry.getValue())) {
									return lineageMap.get(entry.getValue())
											.toBuilder().setTarget(oColumn).build();
								}
							}

							return LineageMessages.ColumnLineage.newBuilder()
									.setTarget(oColumn)
									.addAllParents(ImmutableSet.of(entry.getValue()))
									.build();
						})
				.collect(toImmutableSet());
	}

	private ImmutableSet<ImmutableMap<LineageMessages.ColumnEntity, LineageMessages.ColumnLineage>> extractLineages(
			ImmutableSet<ColumnLineageExtractor> extractors) {
		return extractors.stream()
				.map(ColumnLineageExtractor::extract)
				.filter(Objects::nonNull)
				.collect(toImmutableSet());
	}

	private static ImmutableSet<String> extractReferencedTables(String sql) {
		return extractTableNamesFromStatement(sql).stream()
				.flatMap(List::stream)
				.collect(toImmutableSet());
	}

	private ResolvedNodes.ResolvedStatement resolve(String sql) {
		return Analyzer.analyzeStatement(sql, enableAllFeatures(), buildCatalogWithQueryTables(sql));
	}

	private AnalyzerOptions enableAllFeatures() {
		AnalyzerOptions analyzerOptions = new AnalyzerOptions();
		analyzerOptions.setLanguageOptions(new LanguageOptions().enableMaximumLanguageFeatures());
		analyzerOptions.setPruneUnusedColumns(true);

		return analyzerOptions;
	}

	/**
	 * Creates a ZetaSQL Catalog instance with Table schema (for referenced tables) loaded using the
	 * provided SchemaLoader.
	 *
	 * @param sql the SQL Statement to load referenced tables schemas for.
	 */
	private SimpleCatalog buildCatalogWithQueryTables(String sql) {
		SimpleCatalog catalog = new SimpleCatalog("queryCatalog");
		catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());

		if (tableSchemaLoader != null) {
			tableSchemaLoader.loadSchemas(extractReferencedTables(sql))
					.forEach(catalog::addSimpleTable);
		}

		return catalog;
	}
}