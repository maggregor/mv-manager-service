package com.alwaysmart.optimizer.services;

import com.alwaysmart.optimizer.configuration.SimpleGoogleCredentialsAuthentication;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryDatabaseFetcher;
import com.alwaysmart.optimizer.databases.DatabaseFetcher;
import com.alwaysmart.optimizer.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.alwaysmart.optimizer.databases.entities.FetchedQuery;
import com.alwaysmart.optimizer.databases.entities.FetchedDataset;
import com.alwaysmart.optimizer.databases.entities.FetchedProject;
import com.alwaysmart.optimizer.databases.entities.FetchedTable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
		return fetcher(projectId).fetchProject(projectId);
	}

	public List<FetchedDataset> fetchAllDatasets(String projectId) {
		return fetcher(projectId).fetchAllDatasets();
	}

	public FetchedDataset fetchDataset(String projectId, String datasetName) {
		return fetcher(projectId).fetchDataset(datasetName);
	}

	public List<FetchedQuery> fetchQueries(String projectId) {
		return fetcher(projectId).fetchAllQueries();
	}

	public FetchedTable fetchTable(String projectId, String datasetName, String tableName) {
		return fetcher(projectId).fetchTable(projectId, datasetName, tableName);
	}

	public List<FetchedTable> fetchAllTables(String projectId) {
		return fetcher(projectId).fetchAllTables();
	}

	private DatabaseFetcher fetcher() {
		return fetcher(null);
	}

	private DatabaseFetcher fetcher(String projectId) {
		SimpleGoogleCredentialsAuthentication authentication = (SimpleGoogleCredentialsAuthentication) SecurityContextHolder.getContext().getAuthentication();
		return new BigQueryDatabaseFetcher(authentication.getCredentials(), projectId);
	}


}