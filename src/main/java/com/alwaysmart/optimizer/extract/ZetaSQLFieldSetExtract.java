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
		registerProjectCatalog(projectName);
		this.registerTables(tables);
	}

	public void registerTables(List<FetchedTable> tables) {
		tables.forEach(this::registerTable);
	}

	private void registerTable(FetchedTable table) {
		final String datasetCatalogName = table.getProjectId() + "." + table.getDatasetName();
		final String fullTableName = table.getProjectId() + "." + table.getDatasetName() + "." + table.getTableName();
		final SimpleCatalog dataset = registerDatasetCatalogIfNotExists(datasetCatalogName);
		final SimpleTable simpleTable = new SimpleTable(fullTableName);
		for(Map.Entry<String, String> column : table.getColumns().entrySet()) {
			final String name = column.getKey();
			String typeName = column.getValue();
			ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
			Type type = TypeFactory.createSimpleType(typeKind);
			simpleTable.addSimpleColumn(name, type);
		}
		try {
			// Temp. hack //
			List<String> path = new ArrayList<>();
			if(StringUtils.isNotEmpty(table.getProjectId())) {
				path.add(table.getProjectId());
			}
			if(StringUtils.isNotEmpty(table.getDatasetName())) {
				path.add(table.getDatasetName());
			}
			if(StringUtils.isNotEmpty(table.getTableName())) {
				path.add(table.getTableName());
			}
			String tableName = path.get(path.size()-1);
			path.clear();
			path.add(tableName);
			dataset.findTable(path);
		} catch (Exception e) {
			dataset.addSimpleTable(simpleTable);
		}
	}

	private void registerProjectCatalog(String project) {
		this.catalog = new SimpleCatalog(project);
		this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
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
		List<List<String>> allTables = null;
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
