package com.achilio.mvm.service.entities;

import static java.lang.String.format;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * To sync columns during new fetching, we need to delete table that are not present anymore, and
 * create the new ones, while not touching to existing ones that are still present. This will be
 * done like this:
 *
 * <p>List<Integer> ints1 = Arrays.asList(1, 2, 3); System.out.println(ints1); List<Integer> ints2 =
 * Arrays.asList(2, 3, 4); System.out.println(ints1);
 *
 * <p>List<Integer> toDelete = new ArrayList<>(ints1); toDelete.removeAll(ints2);
 * System.out.println(toDelete);
 *
 * <p>List<Integer> toCreate = new ArrayList<>(ints2); toCreate.removeAll(ints1);
 * System.out.println(toCreate);
 */
@Entity
@Table(name = "imported_columns")
public class ImportedColumn {

  @ManyToOne ImportedTable table;

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column private String name;

  @Column private String type;

  public ImportedColumn() {}

  public ImportedColumn(ImportedTable table, String name, String type) {
    this.table = table;
    this.name = name;
    this.type = type;
    setId(table, name);
  }

  public ImportedColumn(String id, ImportedTable table, String name, String type) {
    this.id = id;
    this.table = table;
    this.name = name;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setId(ImportedTable table, String name) {
    this.id = format("%s:%s:%s#%s", table.getProject().getProjectId(), table.getDataset().getDatasetName(), table.getTableName(), name);
  }

  public ImportedTable getTable() {
    return table;
  }

  public void setTable(ImportedTable table) {
    this.table = table;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
