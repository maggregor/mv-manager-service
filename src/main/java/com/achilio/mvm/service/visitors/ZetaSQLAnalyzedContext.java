package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.google.zetasql.NotFoundException;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SimpleTable;
import com.google.zetasql.Type;
import com.google.zetasql.TypeFactory;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ZetaSQLAnalyzedContext implements TableModelBuilder {

  private final SimpleCatalog catalog;
  private final Set<FetchedTable> tables;

  public ZetaSQLAnalyzedContext(String projectName, Set<FetchedTable> tables) {
    this.catalog = new SimpleCatalog(projectName);
    this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
    this.tables = tables;
    this.registerTables(tables);
  }

  public Set<FetchedTable> tables() {
    return this.tables;
  }

  @Override
  public void registerTables(Set<FetchedTable> tables) {
    tables.forEach(this::registerTable);
  }

  @Override
  public void registerTable(FetchedTable table) {
    final String datasetName = table.getDatasetName();
    ensureDatasetExists(datasetName);
    final String tableName = table.getTableName();
    final SimpleCatalog dataset = getDatasetCatalog(datasetName);
    final String fullTableName = datasetName + "." + tableName;
    final SimpleTable simpleTable = new SimpleTable(fullTableName);
    for (Map.Entry<String, String> column : table.getColumns().entrySet()) {
      final String name = column.getKey();
      final String typeName = column.getValue();
      final ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
      final Type type = TypeFactory.createSimpleType(typeKind);
      simpleTable.addSimpleColumn(name, type);
    }
    dataset.addSimpleTable(tableName, simpleTable);
  }

  @Override
  public boolean isTableRegistered(FetchedTable table) {
    try {
      List<String> paths = new ArrayList<>();
      paths.add(table.getDatasetName());
      paths.add(table.getTableName());
      this.catalog.findTable(paths);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }

  private void ensureDatasetExists(final String datasetName) {
    if (!containsDataset(datasetName)) {
      this.catalog.addNewSimpleCatalog(datasetName);
    }
  }

  private boolean containsDataset(String datasetName) {
    return this.catalog.getCatalogList().stream()
        .anyMatch(simpleCatalog -> simpleCatalog.getFullName().equalsIgnoreCase(datasetName));
  }

  private SimpleCatalog getDatasetCatalog(String datasetName) {
    for (SimpleCatalog catalog : this.catalog.getCatalogList()) {
      if (catalog.getFullName().equalsIgnoreCase(datasetName)) {
        return catalog;
      }
    }
    return null;
  }

  public SimpleCatalog getCatalog() {
    return this.catalog;
  }

}
