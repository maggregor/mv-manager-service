package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;
import com.google.zetasql.Analyzer;
import com.google.zetasql.AnalyzerOptions;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SimpleTable;
import com.google.zetasql.Type;
import com.google.zetasql.TypeFactory;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.ZetaSQLType;
import com.google.zetasql.resolvedast.ResolvedNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZetaSQLFieldSetExtract implements FieldSetExtract {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLFieldSetExtract.class);
	private final AnalyzerOptions options = new AnalyzerOptions();
	private SimpleCatalog catalog;


	public ZetaSQLFieldSetExtract(final TableMetadata metadata) {
		initializeCatalog(metadata);
	}

	private void initializeCatalog(TableMetadata metadata) {
		this.catalog = new SimpleCatalog(metadata.schema());
		this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
		SimpleTable simpleTable = new SimpleTable(metadata.table());
		for(Map.Entry<String, String> column : metadata.columns().entrySet()) {
			final String name = column.getKey();
			final String typeName = column.getValue();
			ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf("TYPE_" + typeName.toUpperCase());
			Type type = TypeFactory.createSimpleType(typeKind);
			simpleTable.addSimpleColumn(name, type);
		}
		this.catalog.addSimpleTable(simpleTable);
	}

	@Override
	public List<FieldSet> extract(List<FetchedQuery> fetchedQueries) {
		List<FieldSet> fieldSets = new LinkedList<>();
		for (FetchedQuery query : fetchedQueries) {
			fieldSets.add(extract(query));
		}
		return fieldSets;
	}

	@Override
	public FieldSet extract(FetchedQuery fetchedQuery) {
		Analyzer analyzer = new Analyzer(options, catalog);
		ResolvedNodes.ResolvedStatement statement = analyzer.analyzeStatement(fetchedQuery.statement());
		ZetaSQLFieldSetExtractVisitor extractVisitor = new ZetaSQLFieldSetExtractVisitor();
		statement.accept(extractVisitor);
		return extractVisitor.fieldSet();
	}

}
