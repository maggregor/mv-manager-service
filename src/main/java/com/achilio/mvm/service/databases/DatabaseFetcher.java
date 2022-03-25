package com.achilio.mvm.service.databases;

import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedOrganization;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
import com.achilio.mvm.service.entities.AOrganization;
import com.achilio.mvm.service.exceptions.ProjectNotFoundException;
import java.util.List;
import java.util.Set;

/** Database fetcher interface */
public interface DatabaseFetcher {

  /**
   * Returns history queries for a given table.
   *
   * @return - a list of queries as string
   */
  List<FetchedQuery> fetchAllQueries();

  /**
   * Returns history queries for a given table and date range.
   *
   * @param fromTimestamp - timestamp start
   * @return - a list of queries as string
   */
  List<FetchedQuery> fetchAllQueriesFrom(long fromTimestamp);

  /**
   * Returns metadata for a given table.
   *
   * @param datasetName
   * @param tableName
   * @return - FetchedTable of targeted table
   */
  FetchedTable fetchTable(String datasetName, String tableName);

  /**
   * Returns all projects id.
   *
   * @return
   */
  List<FetchedProject> fetchAllProjects();

  /**
   * Returns all the information for a given project.
   *
   * @return
   */
  FetchedProject fetchProject(String projectId) throws ProjectNotFoundException;

  /**
   * Returns all dataset id in a given projectId.
   *
   * @return
   */
  List<FetchedDataset> fetchAllDatasets(String projectId);

  /**
   * Returns the dataset metadata in a given projectId.
   *
   * @param datasetName
   * @return
   */
  FetchedDataset fetchDataset(String datasetName);

  Set<FetchedTable> fetchAllTables();

  Set<FetchedTable> fetchTablesInDataset(String datasetName);

  List<String> fetchMissingPermissions(String projectId);

  List<FetchedOrganization> fetchAllOrganizations();

  List<FetchedProject> fetchAllProjectsFromOrg(AOrganization baseOrganization);

  List<FetchedProject> fetchAllProjectsFromParent(String parentId, AOrganization baseOrganization);

  List<FetchedProject> fetchAllProjectsNoParent();

  void close();
}
