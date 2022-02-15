package com.achilio.mvm.service.databases.entities;

import java.util.Map;

/** Represents a single table from database with metadata. */
public interface FetchedTable {

  /**
   * The project name of the table on which metadata was retrieved.
   *
   * @return the project name of the table on which metadata was retrieved.
   */
  String getProjectId();

  /**
   * The dataset of the table on which metadata was retrieved. Example: {@code default}
   *
   * @return the dataset name of the table on which metadata was retrieved.
   */
  String getDatasetName();

  /**
   * The name of the table on which metadata was retrieved. Example: {@code my_table}
   *
   * @return the name of the table on which metadata was retrieved.
   */
  String getTableName();

  /**
   * The name and type of the columns of the table. Example: {@code [{col1, STRING}, {col2,
   * INTEGER}]
   *
   * @return the name and type of the columns of the table.
   */
  Map<String, String> getColumns();
}
