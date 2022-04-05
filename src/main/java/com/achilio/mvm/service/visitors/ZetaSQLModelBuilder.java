package com.achilio.mvm.service.visitors;

import com.achilio.mvm.service.entities.AColumn;
import com.achilio.mvm.service.entities.ATable;
import com.google.zetasql.SimpleCatalog;
import com.google.zetasql.SimpleTable;
import com.google.zetasql.Type;
import com.google.zetasql.TypeFactory;
import com.google.zetasql.ZetaSQLBuiltinFunctionOptions;
import com.google.zetasql.ZetaSQLType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

public abstract class ZetaSQLModelBuilder implements ModelBuilder {

  private static final String CATALOG_ROOT_NAME = "root";
  private final SimpleCatalog rootCatalog = new SimpleCatalog(CATALOG_ROOT_NAME);
  private final Set<ATable> tables;

  public ZetaSQLModelBuilder() {
    this(Collections.emptySet());
  }

  public ZetaSQLModelBuilder(Set<ATable> tables) {
    this.rootCatalog.addZetaSQLFunctions(new ZetaSQLBuiltinFunctionOptions());
    this.tables = tables;
    this.registerTables(tables);
  }

  public Set<ATable> tables() {
    return this.tables;
  }

  @Override
  public void registerTable(ATable table) {
    SimpleCatalog dataset = createDatasetAndGet(rootCatalog, table.getDatasetName());
    registerTable(dataset, table);
    if (Strings.isNotBlank(table.getProjectId())) {
      SimpleCatalog catalogProject = createDatasetAndGet(rootCatalog, table.getProjectId());
      SimpleCatalog datasetInProject = createDatasetAndGet(catalogProject, table.getDatasetName());
      registerTable(datasetInProject, table);
    }
  }

  public void registerTable(SimpleCatalog catalog, ATable table) {
    final String tableName = table.getTableName();
    final String fullTableName = table.getDatasetName() + "." + tableName;
    final SimpleTable simpleTable = new SimpleTable(fullTableName);
    for (AColumn column : table.getColumns()) {
      final String name = column.getName();
      final String typeName = column.getType();
      final ZetaSQLType.TypeKind typeKind = ZetaSQLType.TypeKind.valueOf(typeName);
      final Type statusType = TypeFactory.createSimpleType(typeKind);
      simpleTable.addSimpleColumn(name, statusType);
    }
    catalog.addSimpleTable(tableName, simpleTable);
  }

  public void setDefaultDataset(String datasetName) {
    getRootCatalog().getTableNameList().forEach(name -> getRootCatalog().removeSimpleTable(name));
    tables.stream()
        .filter(table -> table.getDatasetName().equalsIgnoreCase(datasetName))
        .forEach(table -> registerTable(getRootCatalog(), table));
  }

  @Override
  public boolean isTableRegistered(ATable table) {
    try {
      List<String> paths = new ArrayList<>();
      if (StringUtils.isNotEmpty(table.getProjectId())) {
        paths.add(table.getProjectId());
      }
      paths.add(table.getDatasetName());
      paths.add(table.getTableName());
      this.rootCatalog.findTable(paths);
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

  public SimpleCatalog getRootCatalog() {
    return this.rootCatalog;
  }

  @Override
  public Set<ATable> getTables() {
    return tables;
  }
}
