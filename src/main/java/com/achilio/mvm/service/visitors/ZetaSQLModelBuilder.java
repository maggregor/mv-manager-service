package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SimpleTable;
import com.google.zetasql.Type;
import com.google.zetasql.TypeFactory;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public abstract class ZetaSQLModelBuilder implements ModelBuilder {

  private final SimpleCatalog catalog;
  private final SimpleCatalog catalogProject;
  private final Set<FetchedTable> tables;
  private final String projectId;

  public ZetaSQLModelBuilder(String projectId, Set<FetchedTable> tables) {
    this.catalog = new SimpleCatalog("root");
    this.projectId = projectId;
    catalogProject = this.catalog.addNewSimpleCatalog(projectId);
    this.catalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
    this.tables = tables;
    this.registerTables(tables);
  }

  public Set<FetchedTable> tables() {
    return this.tables;
  }

  @Override
  public void registerTable(FetchedTable table) {
    ATableId tableId = table.getTableId();
    SimpleCatalog dataset = createDatasetAndGet(catalog, tableId.getDataset());
    SimpleCatalog datasetInProject = createDatasetAndGet(catalogProject, tableId.getDataset());
    registerTable(dataset, table);
    registerTable(datasetInProject, table);
  }

  public void registerTable(SimpleCatalog catalog, FetchedTable table) {
    ATableId tableId = table.getTableId();
    final String tableName = tableId.getTable();
    final String fullTableName = tableId.getDataset() + "." + tableName;
    final SimpleTable simpleTable = new SimpleTable(fullTableName);
    for (Map.Entry<String, String> column : table.getColumns().entrySet()) {
      final String name = column.getKey();
      final String typeName = column.getValue();
      final ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
      final Type statusType = TypeFactory.createSimpleType(typeKind);
      simpleTable.addSimpleColumn(name, statusType);
    }
    catalog.addSimpleTable(tableName, simpleTable);
  }

  public void setDefaultDataset(String datasetName) {
    getCatalog().getTableNameList().forEach(name -> getCatalog().removeSimpleTable(name));
    tables.stream()
        .filter(table -> table.getTableId().getDataset().equalsIgnoreCase(datasetName))
        .forEach(table -> registerTable(getCatalog(), table));
  }

  @Override
  public boolean isTableRegistered(FetchedTable table) {
    try {
      ATableId tableId = table.getTableId();
      List<String> paths = new ArrayList<>();
      if (StringUtils.isNotEmpty(table.getProjectId())) {
        paths.add(tableId.getProject());
      }
      paths.add(tableId.getDataset());
      paths.add(tableId.getTable());
      this.catalog.findTable(paths);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private SimpleCatalog createDatasetAndGet(SimpleCatalog simpleCatalog, final String datasetName) {
    Optional<SimpleCatalog> optionalCatalog = getDataset(simpleCatalog, datasetName);
    return optionalCatalog.orElseGet(() -> simpleCatalog.addNewSimpleCatalog(datasetName));
  }

  private Optional<SimpleCatalog> getDataset(SimpleCatalog catalog, String datasetName) {
    return catalog.getCatalogList().stream()
        .filter(simpleCatalog -> simpleCatalog.getFullName().equalsIgnoreCase(datasetName))
        .findFirst();
  }

  public SimpleCatalog getCatalog() {
    return this.catalog;
  }

  public String getProjectId() {
    return this.projectId;
  }

  @Override
  public Set<FetchedTable> getTables() {
    return tables;
  }
}
