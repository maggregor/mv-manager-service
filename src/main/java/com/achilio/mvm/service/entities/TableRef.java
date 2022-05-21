package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.visitors.ATableId;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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

  public enum TableRefType {
    MAIN,
    INNER,
    LEFT,
    RIGHT,
    FULL,
    CROSS
  }

}
