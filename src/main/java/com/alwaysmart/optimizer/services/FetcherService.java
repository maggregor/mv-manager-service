package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.configuration.SimpleGoogleCredentialsAuthentication;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedDataset;
import com.alwaysmart.optimizer.databases.entities.FetchedProject;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import com.google.cloud.bigquery.TableId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All the useful services to generate relevant Materialized Views.
 */
@Service
public class FetcherService {

	BigQueryMaterializedViewStatementBuilder statementBuilder;

	@PersistenceContext
	private EntityManager entityManager;

	public FetcherService() {
		this.statementBuilder = new BigQueryMaterializedViewStatementBuilder();
	}

	public List<String> fetchAllProjectsIds() {
		return fetcher().fetchProjectIds();
	}

	public List<FetchedProject> fetchAllProjects() {
		return fetchAllProjectsIds()
				.stream()
				.map(this::fetchProject)
				.collect(Collectors.toList());
	}

	public FetchedProject fetchProject(String projectId) {
		return fetcher().fetchProject(projectId);
	}

	public List<String> fetchAllDatasetIds(String projectId) {
		return fetcher().fetchDatasetIds(projectId);
	}

	public FetchedDataset fetchDataset(String datasetId) {
		return fetcher().fetchDataset(datasetId);
	}

	public List<String> fetchAllTableIds(String datasetId) {
		return fetcher().fetchTableIds(datasetId);
	}

	public List<FetchedQuery> fetchQueries(String projectId) {
		return fetcher(projectId).fetchQueries(projectId);
	}

	public FetchedTable fetchTable(String tableId) {
		return fetcher().fetchTable(tableId);
	}

	public List<FetchedTable> fetchTables(Collection<TableId> tableIds) {
		return fetcher().fetchTables(tableIds);
	}

	private DatabaseFetcher fetcher() {
		return fetcher(null);
	}

	private DatabaseFetcher fetcher(String projectId) {
		SimpleGoogleCredentialsAuthentication authentication = (SimpleGoogleCredentialsAuthentication) SecurityContextHolder.getContext().getAuthentication();
		return new BigQueryDatabaseFetcher(authentication.getCredentials(), projectId);
	}


}