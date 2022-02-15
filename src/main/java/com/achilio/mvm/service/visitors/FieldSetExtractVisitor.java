package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.visitors.fields.DefaultFieldSet;
import com.achilio.mvm.service.visitors.fields.Field;
import com.achilio.mvm.service.visitors.fields.FieldSet;
import com.google.zetasql.resolvedast.ResolvedNode;
import com.google.zetasql.resolvedast.ResolvedNodes;

public abstract class FieldSetExtractVisitor extends ResolvedNodes.Visitor {

  private final FieldSet fieldSet = new DefaultFieldSet();

  /**
   * Allow the children class to specify special filter
   *
   * @return True if allowed
   */
  public abstract boolean filterAllowAddField(Field field);

  @Override
  protected void defaultVisit(ResolvedNode node) {
    super.defaultVisit(node);
  }

  public void addField(Field field) {
    if (filterAllowAddField(field)) {
      this.fieldSet.add(field);
    }
  }

  public void merge(FieldSet fieldSet) {
    this.fieldSet.merge(fieldSet);
  }

  public FieldSet fieldSet() {
    return fieldSet;
  }
}
