package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ADataset;
import com.achilio.mvm.service.entities.ATable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ADatasetEntitiesHolder {

  private ADataset dataset;
  private List<ATable> tables = new ArrayList<>();
  private List<AColumn> columns = new ArrayList<>();

  public ADatasetEntitiesHolder(ADataset dataset) {
    this.dataset = dataset;
  }

  public ADataset getDataset() {
    return dataset;
  }

  public void addTable(ATable table) {
    this.tables.add(table);
  }
  
  public void addColumns(List<AColumn> columns) {
    this.columns.addAll(columns);
  }

}
