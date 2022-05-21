package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.Field;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.entities.TableRef;
import com.achilio.mvm.service.entities.TableRef.TableRefType;
import com.google.zetasql.resolvedast.ResolvedNodes;

public abstract class FieldSetExtractVisitor extends ResolvedNodes.Visitor {

  private final QueryPattern queryPattern;

  public FieldSetExtractVisitor() {
    this.queryPattern = new QueryPattern();
  }

  /**
   * Allow the children class to specify special filter
   *
   * @return True if allowed
   */
  public abstract boolean filterAllowAddField(Field field);

  public void addField(Field field) {
    if (filterAllowAddField(field)) {
      this.queryPattern.add(field);
    }
  }

  public void setTableReference(ATableId tableId) {
    this.queryPattern.addTableRef(new TableRef(tableId, TableRefType.MAIN));
  }

  public void addTableJoin(ATableId tableId, TableRefType type) {
    this.queryPattern.addTableRef(new TableRef(tableId, type));
  }

  public QueryPattern getQueryPattern() {
    return queryPattern;
  }
}
