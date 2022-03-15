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
    SimpleCatalog dataset = createDatasetAndGet(catalog, table.getDatasetName());
    SimpleCatalog datasetInProject = createDatasetAndGet(catalogProject, table.getDatasetName());
    final String tableName = table.getTableName();
    final String fullTableName = table.getDatasetName() + "." + tableName;
    final SimpleTable simpleTable = new SimpleTable(fullTableName);
    for (Map.Entry<String, String> column : table.getColumns().entrySet()) {
      final String name = column.getKey();
      final String typeName = column.getValue();
      final ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
      final Type statusType = TypeFactory.createSimpleType(typeKind);
      simpleTable.addSimpleColumn(name, statusType);
    }
    dataset.addSimpleTable(tableName, simpleTable);
    datasetInProject.addSimpleTable(tableName, simpleTable);
  }

  @Override
  public boolean isTableRegistered(FetchedTable table) {
    try {
      List<String> paths = new ArrayList<>();
      if (StringUtils.isNotEmpty(table.getProjectId())) {
        paths.add(table.getProjectId());
      }
      paths.add(table.getDatasetName());
      paths.add(table.getTableName());
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
}
