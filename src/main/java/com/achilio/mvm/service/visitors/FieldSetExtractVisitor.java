package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.achilio.mvm.service.visitors.fields.FieldSetFactory;
import com.google.zetasql.resolvedast.ResolvedNodes;

public abstract class FieldSetExtractVisitor extends ResolvedNodes.Visitor {

  private final FieldSet fieldSet;

  public FieldSetExtractVisitor() {
    this.fieldSet = FieldSetFactory.createFieldSet();
  }

  /**
   * Allow the children class to specify special filter
   *
   * @return True if allowed
   */
  public abstract boolean filterAllowAddField(Field field);

  public void addField(Field field) {
    if (filterAllowAddField(field)) {
      this.fieldSet.add(field);
    }
  }

  public void setTableReference(ATableId tableId) {
    this.fieldSet.setReferenceTable(tableId);
  }

  public void addTableJoin(ATableId tableId, JoinType type) {
    this.fieldSet.addJoinTable(tableId, type);
  }

  public FieldSet getFieldSet() {
    return fieldSet;
  }
}
