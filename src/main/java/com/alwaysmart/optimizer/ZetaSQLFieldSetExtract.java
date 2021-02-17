package com.alwaysmart.optimizer;

import com.alwaysmart.optimizer.fields.Field;
import com.alwaysmart.optimizer.fields.FieldSet;
import com.alwaysmart.optimizer.fields.ReferenceField;
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

	@Override
	public FieldSet extract(FetchedQuery fetchedQuery, TableMetadata metadata) {
		AnalyzerOptions options = new AnalyzerOptions();
		SimpleCatalog catalog = new SimpleCatalog(metadata.getSchema());
		catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
		SimpleTable simpleTable = new SimpleTable(metadata.getTable());
		FieldSet fieldSet = new FieldSet();

		for(Map.Entry<String, String> column : metadata.getColumns().entrySet()) {
			String name = column.getKey();
			String typeName = column.getValue();
			ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf("TYPE_" + typeName.toUpperCase());
			Type type = TypeFactory.createSimpleType(typeKind);
			simpleTable.addSimpleColumn(name, type);

		}

		catalog.addSimpleTable(simpleTable);
		Analyzer analyzer = new Analyzer(options, catalog);
		ResolvedNodes.ResolvedStatement statement = analyzer.analyzeStatement(fetchedQuery.getStatement());
		statement.accept(new ResolvedNodes.Visitor(){
			public void visit(ResolvedNodes.ResolvedOutputColumn node) {
				Field field = new ReferenceField(node.getName());
				fieldSet.addField(field);
				super.visit(node);
			}
		});
		return fieldSet;
	}


	@Override
	public List<FieldSet> extract(List<FetchedQuery> fetchedQueries, TableMetadata metadata) {
		List<FieldSet> fieldSets = new LinkedList<>();
		for (FetchedQuery query : fetchedQueries) {
			fieldSets.add(extract(query, metadata));
		}
		return fieldSets;
	}

}
