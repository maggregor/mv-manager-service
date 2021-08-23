package com.alwaysmart.optimizer.extract;

import com.alwaysmart.optimizer.databases.bigquery.BigQueryHelper;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.alwaysmart.optimizer.extract.fields.FieldSetFactory;
import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
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
import com.google.zetasql.ZetaSQLType;
import com.google.zetasql.resolvedast.ResolvedNodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	public ZetaSQLFieldSetExtract(String projectName) {
		this(projectName, new ArrayList<>());
	}

	public ZetaSQLFieldSetExtract(String projectName, List<FetchedTable> tables) {
		this.catalog = new SimpleCatalog(projectName);
		this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
		this.registerTables(tables);
	}

	@Override
	public void registerTables(List<FetchedTable> tables) {
		tables.forEach(this::registerTable);
	}

	@Override
	public void registerTable(FetchedTable table) {
		final String datasetName = table.getDatasetName();
		ensureDatasetExists(datasetName);
		final String tableName = table.getTableName();
		final SimpleCatalog dataset = getDatasetCatalog(datasetName);
		final String fullTableName = datasetName + "." + tableName;
		final SimpleTable simpleTable = new SimpleTable(fullTableName);
		for(Map.Entry<String, String> column : table.getColumns().entrySet()) {
			final String name = column.getKey();
			final String typeName = column.getValue();
			final ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
			final Type type = TypeFactory.createSimpleType(typeKind);
			simpleTable.addSimpleColumn(name, type);
		}
		dataset.addSimpleTable(tableName, simpleTable);
	}

	private void ensureDatasetExists(final String datasetName) {
		if (!this.catalog.getCatalogList().contains(datasetName)) {
			this.catalog.addNewSimpleCatalog(datasetName);
		}
	}

	@Override
	public boolean isTableRegistered(final String datasetName, final String tableName) {
		try {
			List<String> paths = new ArrayList<>();
			paths.add(datasetName);
			paths.add(tableName);
			this.catalog.findTable(paths);
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}

	private SimpleCatalog getDatasetCatalog(String datasetName) {
		for(SimpleCatalog catalog : this.catalog.getCatalogList()) {
			if(catalog.getFullName().equalsIgnoreCase(datasetName)) {
				return catalog;
			}
		}
		return null;
	}

	@Override
	public FieldSet extract(FetchedQuery fetchedQuery) {
		Preconditions.checkNotNull(this.catalog, "Catalog initialization is required: register tables.");
		final String statement = fetchedQuery.statement();
		try {
			ResolvedNodes.ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
			ZetaSQLFieldSetExtractGlobalVisitor extractVisitor = new ZetaSQLFieldSetExtractGlobalVisitor(catalog);
			resolvedStatement.accept(extractVisitor);
			// TODO: Remove references.
			/*containsAllReferences(fetchedQuery.getTableId(), fieldSet) ? FieldSetFactory.EMPTY_FIELD_SET : */
			return extractVisitor.fieldSet();
		} catch (Exception e) {
			return FieldSetFactory.EMPTY_FIELD_SET;
		}
	}

	@Override
	public Set<TableId> extractTableId(FetchedQuery fetchedQuery) {
		Set<TableId> tableIds = new HashSet<>();
		List<List<String>> allTables;
		try {
			allTables = extractTableNamesFromStatement(fetchedQuery.statement(), options);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
			return new HashSet<>();
		}
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

}
