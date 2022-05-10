package com.achilio.mvm.service.entities;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.zetasql.ZetaSQLType.TypeKind;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("bigquery")
public class BigQueryColumn extends AColumn {


  public BigQueryColumn(String projectId, String tableId, Field field) {
    super(projectId, tableId, field.getName());
    this.setType(toZetaSQLType(field).name());
  }

  public BigQueryColumn() {

  }


  /**
   * Columns type mapping. Between BigQuery enum TypeKind and ZetaSQL type.
   *
   * @param field
   * @return
   */
  private TypeKind toZetaSQLType(Field field) {
    final StandardSQLTypeName statusType = field.getType().getStandardType();
    switch (statusType) {
      case FLOAT64:
        return TypeKind.TYPE_FLOAT;
      case NUMERIC:
        return TypeKind.TYPE_NUMERIC;
      case BOOL:
        return TypeKind.TYPE_BOOL;
      case DATE:
        return TypeKind.TYPE_DATE;
      case TIME:
        return TypeKind.TYPE_TIME;
      case TIMESTAMP:
        return TypeKind.TYPE_TIMESTAMP;
      case BYTES:
        return TypeKind.TYPE_BYTES;
      case ARRAY:
        return TypeKind.TYPE_ARRAY;
      case INT64:
        return TypeKind.TYPE_UINT64;
      case DATETIME:
        return TypeKind.TYPE_DATETIME;
      case STRUCT:
        return TypeKind.TYPE_STRUCT;
      case GEOGRAPHY:
        return TypeKind.TYPE_GEOGRAPHY;
      case BIGNUMERIC:
        return TypeKind.TYPE_BIGNUMERIC;
      case STRING:
        return TypeKind.TYPE_STRING;
    }
    throw new IllegalArgumentException("Unsupported column type  " + statusType);
  }

}
