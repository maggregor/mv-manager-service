package com.alwaysmart.optimizer.databases.bigquery;

import com.alwaysmart.optimizer.databases.MaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.extract.fields.Field;
import com.alwaysmart.optimizer.extract.fields.FieldSet;
import com.google.cloud.bigquery.TableId;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

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

	@Override
	public String build(FieldSet fieldSet) {
		final StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(buildSelect(fieldSet));
		joiner.add(buildFrom(fieldSet));
		joiner.add(buildGroupBy(fieldSet));
		return joiner.toString();
	}

	public String buildColumns(Set<Field> fields, boolean addAlias) {
		StringJoiner columns = new StringJoiner(SEP_COLUMNS);
		fields.forEach(field -> columns.add(serializeField(field, addAlias)));
		return columns.toString();
	}

	public String serializeField(Field field, boolean addAlias) {
		StringJoiner alias = new StringJoiner(SEP_SQL_VERBS);
		alias.add(field.name());
		if (addAlias) {
			alias.add(SQL_VERB_AS);
			alias.add(generateRandomAlias());
		}
		return alias.toString();
	}

	/**
	 * Alias who respect BigQuery rules for column name.
	 *
	 * @return a random alias compatible with BigQuery syntax.
	 */
	public String generateRandomAlias() {
		String uuid = UUID.randomUUID().toString();
		Random r = new Random();
		char c = (char) (r.nextInt(26) + 'a');
		uuid = uuid.replaceAll("[a-z]{0}", String.valueOf(c));
		uuid = uuid.replaceAll("-", StringUtils.EMPTY);
		return uuid;
	}

	public String buildSelect(FieldSet fieldSet) {
		StringJoiner joiner = new StringJoiner(SEP_SQL_VERBS);
		joiner.add(SQL_VERB_SELECT);
		joiner.add(buildColumns(fieldSet.references(), true));
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
		Preconditions.checkNotNull(tableId);
		if (StringUtils.isEmpty(tableId.getProject())) {
			throw new IllegalArgumentException("Project name is empty or null");
		}
		return String.format("`%s`.`%s`.`%s`", tableId.getProject(), tableId.getDataset(), tableId.getTable());
	}

}
