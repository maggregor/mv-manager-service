package com.achilio.mvm.service.services;

import com.achilio.mvm.service.configuration.SimpleGoogleCredentialsAuthentication;
import com.achilio.mvm.service.databases.DatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.databases.bigquery.BigQueryMaterializedViewStatementBuilder;
import com.achilio.mvm.service.databases.entities.FetchedDataset;
import com.achilio.mvm.service.databases.entities.FetchedProject;
import com.achilio.mvm.service.databases.entities.FetchedQuery;
import com.achilio.mvm.service.databases.entities.FetchedTable;
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