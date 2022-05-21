package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TableRef {

  @Embedded
  private ATableId table;
  @Enumerated(EnumType.STRING)
  private TableRefType origin;

  public ATableId getTable() {
    return table;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TableRef)) {
      return false;
    }

    TableRef tableRef = (TableRef) o;

    if (!Objects.equals(table, tableRef.table)) {
      return false;
    }
    return origin == tableRef.origin;
  }

  @Override
  public int hashCode() {
    int result = table != null ? table.hashCode() : 0;
    result = 31 * result + (origin != null ? origin.hashCode() : 0);
    return result;
  }

  public enum TableRefType {
    MAIN,
    INNER,
    LEFT,
    RIGHT,
    FULL,
    CROSS
  }
}
