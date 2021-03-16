package com.alwaysmart.optimizer.services;

import java.util.List;

import com.alwaysmart.optimizer.TableMetadata;

public interface IOptimizerService {
    List<String> getProjects(/* CREDENTIALS */);

    List<String> getDatasets(/* CREDENTIALS */String project);

    List<String> getTables(/* CREDENTIALS */String project, String dataset);

    List<TableMetadata> getTableMetadata(/* CREDENTIALS */String project, String dataset, String table);
}
