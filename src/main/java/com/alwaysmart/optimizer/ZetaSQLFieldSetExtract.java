package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.FieldSetFactory;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.solutions.datalineage.BigQuerySqlParser;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.LanguageOptions;
import com.google.zetasql.NotFoundException;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SimpleTable;
import com.google.zetasql.Table;
import com.google.zetasql.Type;
import com.google.zetasql.TypeFactory;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.ZetaSQLOptions;
import com.google.zetasql.ZetaSQLResolvedNodeKind;
import com.google.zetasql.ZetaSQLType;
import com.google.zetasql.resolvedast.ResolvedNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.zetasql.Analyzer.extractTableNamesFromStatement;

public class ZetaSQLFieldSetExtract implements FieldSetExtract {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLFieldSetExtract.class);
	private final AnalyzerOptions options = new AnalyzerOptions();
	private SimpleCatalog catalog;

	{
		options.getLanguageOptions()
				.enableLanguageFeature(ZetaSQLOptions.LanguageFeature.FEATURE_V_1_3_ALLOW_DASHES_IN_TABLE_NAME);
		options.setLanguageOptions(new LanguageOptions().enableMaximumLanguageFeatures());
		options.setPruneUnusedColumns(true);

	}

	public ZetaSQLFieldSetExtract() {
	}

	// Exposed for testing -- not amazing.
	public ZetaSQLFieldSetExtract(List<TableMetadata> tables) {
		this.registerTables(tables);
	}

	public void registerTables(List<TableMetadata> tables) {
		tables.forEach(this::registerTable);
	}

	private void registerTable(TableMetadata table) {
		final String mainCatalogName = table.getProject();
		final String datasetCatalogName = table.getDataset();
		final String fullTableName = table.getTable();
		registerMainCatalogIfNotExists(mainCatalogName);
		SimpleCatalog dataset = registerDatasetCatalogIfNotExists(datasetCatalogName);
		SimpleTable simpleTable = new SimpleTable(fullTableName);
		for(Map.Entry<String, String> column : table.getColumns().entrySet()) {
			final String name = column.getKey();
			String typeName = column.getValue();
			ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
			Type type = TypeFactory.createSimpleType(typeKind);
			simpleTable.addSimpleColumn(name, type);
		}
		try {
			// Temp. hack //
			List<String> path = BigQueryHelper.parseTableIdToPath(table.getTableId());
			String tableName = path.get(path.size()-1);
			path.clear();
			path.add(tableName);
			dataset.findTable(path);
		} catch (Exception e) {
			dataset.addSimpleTable(simpleTable);
		}
	}

	private void registerMainCatalogIfNotExists(String project) {
		if (this.catalog == null) {
			this.catalog = new SimpleCatalog(project);
			this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
		}
	}

	private SimpleCatalog registerDatasetCatalogIfNotExists(String datasetName) {
		for(SimpleCatalog catalog : this.catalog.getCatalogList()) {
			if(catalog.getFullName().equalsIgnoreCase(datasetName)) {
				return catalog;
			}
		}
		return this.catalog.addNewSimpleCatalog(datasetName);
	}

	@Override
	public FieldSet extract(FetchedQuery fetchedQuery) {
		Preconditions.checkNotNull(this.catalog, "Catalog initialization is required: register tables.");
		final String statement = fetchedQuery.statement();
		try {
			ResolvedNodes.ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
			ZetaSQLFieldSetExtractGlobalVisitor extractVisitor = new ZetaSQLFieldSetExtractGlobalVisitor(catalog);
			resolvedStatement.accept(extractVisitor);
			FieldSet fieldSet = extractVisitor.fieldSet();
			return containsAllReferences(fetchedQuery.getTableId(), fieldSet) ?
					FieldSetFactory.EMPTY_FIELD_SET : fieldSet;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return FieldSetFactory.EMPTY_FIELD_SET;
		}
	}

	@Override
	public Set<TableId> extractTableId(FetchedQuery fetchedQuery) {
		Set<TableId> tableIds = new HashSet<>();
		List<List<String>> allTables = extractTableNamesFromStatement(fetchedQuery.statement(), options);
		for (List<String> table : allTables) {
			try {
				tableIds.add(BigQueryHelper.parseTable(table));
			} catch (Exception e) {
				LOGGER.warn(e.getMessage());
			}
		}
		// Hack to maintain table origin in fetched query.
		if (!tableIds.isEmpty()) {
			fetchedQuery.setTableId(tableIds.iterator().next());
		}
		return tableIds;
	}

	private boolean containsAllReferences(TableId tableId, FieldSet fieldSet) throws NotFoundException {
		Table table = this.catalog.findTable(new ArrayList<String>(){{
			add(tableId.getDataset());
			add(tableId.getTable());
		}});
		return fieldSet.references().size() >= table.getColumnCount();
	}

	private static ImmutableSet<String> extractReferencedTables(String sql) {
		return extractTableNamesFromStatement(sql).stream()
				.flatMap(List::stream)
				.collect(toImmutableSet());
	}

}
