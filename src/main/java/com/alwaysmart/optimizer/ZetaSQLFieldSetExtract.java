package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.FieldSetFactory;
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

import java.util.List;
import java.util.Map;

public class ZetaSQLFieldSetExtract implements FieldSetExtract {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZetaSQLFieldSetExtract.class);
	private final AnalyzerOptions options = new AnalyzerOptions();
	private SimpleCatalog catalog;
	private TableMetadata metadata;

	ZetaSQLFieldSetExtract(final TableMetadata metadata) {
		this.metadata = metadata;
		initializeCatalog();
	}

	private void initializeCatalog() {
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
	public FieldSet extract(FetchedQuery fetchedQuery) {
		final String statement = fetchedQuery.statement();
		ResolvedNodes.ResolvedStatement resolvedStatement = Analyzer.analyzeStatement(statement, options, catalog);
		ZetaSQLFieldSetExtractGlobalVisitor extractVisitor = new ZetaSQLFieldSetExtractGlobalVisitor(catalog);
		resolvedStatement.accept(extractVisitor);
		FieldSet fieldSet = extractVisitor.fieldSet();
		return containsAllReferences(fieldSet) ?
				FieldSetFactory.EMPTY_FIELD_SET : fieldSet;
	}

	private boolean containsAllReferences(FieldSet fieldSet) {
		return fieldSet.references().size() >= metadata.columns().size();
	}

}
