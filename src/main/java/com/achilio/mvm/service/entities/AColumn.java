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
public class AColumn {

  @ManyToOne ATable table;

  @Id private String id;

  @Column private String name;

  @Column private String type;

  public AColumn() {}

  public AColumn(ATable table, String name, String type) {
    this.table = table;
    this.name = name;
    this.type = type;
    setId(table, name);
  }

  public AColumn(String id, ATable table, String name, String type) {
    this.id = id;
    this.table = table;
    this.name = name;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(ATable table, String name) {
    this.id =
        format(
            "%s:%s.%s#%s",
            table.getProject().getProjectId(),
            table.getDataset().getDatasetName(),
            table.getTableName(),
            name);
  }

  public ATable getTable() {
    return table;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }
}
