package com.alwaysmart.optimizer.databases.bigquery;

import com.alwaysmart.optimizer.databases.MaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.extract.fields.Field;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

public class BigQueryMaterializedViewStatementBuilder implements MaterializedViewStatementBuilder {

	private final static String SEP_SQL_VERBS = " ";
	private final static String SEP_COLUMNS = ",";
	private final static String SQL_VERB_SELECT = "SELECT";
	private final static String SQL_VERB_AS = "AS";
	private final static String SQL_VERB_FROM = "FROM";
	private final static String SQL_VERB_GROUP_BY = "GROUP BY";

	private final static String ALIAS_PREFIX = "a_";

	@Override
	public String build(FieldSet fieldSet) {
		final StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(buildSelect(fieldSet));
		joiner.add(buildFrom(fieldSet));
		if (!fieldSet.references().isEmpty()) {
			joiner.add(buildGroupBy(fieldSet));
		}
		return joiner.toString();
	}

	public String buildColumns(Set<Field> fields, boolean addAlias) {
		StringJoiner columns = new StringJoiner(SEP_COLUMNS);
		fields.forEach(field -> columns.add(serializeField(field, addAlias)));
		return columns.toString();
	}

	@Override
	public String serializeField(Field field, boolean addAlias) {
		StringJoiner aliasJoiner = new StringJoiner(SEP_SQL_VERBS);
		String name = field.hasAlias() ? field.alias() : field.name();
		aliasJoiner.add(name);
		if (addAlias && !field.hasAlias()) {
			createStableAlias(field);
			aliasJoiner.add(SQL_VERB_AS);
			aliasJoiner.add(field.alias());
		}
		return aliasJoiner.toString();
	}

	public void createStableAlias(Field field) {
		field.setAlias(ALIAS_PREFIX + Math.abs(field.name().hashCode()));
	}

	public String buildSelect(FieldSet fieldSet) {
		StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(SQL_VERB_SELECT);
		joiner.add(buildColumns(fieldSet.references(), true));
		if (!fieldSet.references().isEmpty()) {
			joiner.add(SEP_COLUMNS);
		}
		joiner.add(buildColumns(fieldSet.aggregates(), true));
		return joiner.toString();
	}

	public String buildFrom(FieldSet fieldSet) {
		StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(SQL_VERB_FROM);
		joiner.add(buildTableReference(fieldSet.getTableId()));
		return joiner.toString();
	}

	public String buildGroupBy(FieldSet fieldSet) {
		StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(SQL_VERB_GROUP_BY);
		joiner.add(buildColumns(fieldSet.references(), false));
		return joiner.toString();
	}

	public String buildTableReference(TableId tableId) {
		return String.format("`%s`.`%s`", tableId.getDataset(), tableId.getTable());
	}

}
