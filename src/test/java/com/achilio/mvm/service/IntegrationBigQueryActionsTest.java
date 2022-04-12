package com.achilio.mvm.service;

import static org.junit.Assert.assertThrows;

import com.achilio.mvm.service.databases.bigquery.BigQueryDatabaseFetcher;
import com.achilio.mvm.service.entities.MaterializedView;
import com.achilio.mvm.service.visitors.ATableId;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.resourcemanager.ResourceManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationBigQueryActionsTest {

  private static final String PROJECT_ID = "achilio-test";
  private static final String DATASET_NAME = "nyc_trips";
  private static final String TABLE_NAME = "tlc_yellow_trips_2015_small";
  private static final String STATEMENT1 =
      String.format(
          "SELECT COUNT(vendor_id) AS a_1642102992 FROM `%s`.`%s`.`%s`",
          PROJECT_ID, DATASET_NAME, TABLE_NAME);
  private static final MaterializedView mv1 =
      new MaterializedView(ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME), STATEMENT1);
  private static final String STATEMENT2 =
      String.format(
          "SELECT COUNT(unknownColumn) AS a_1642102992 FROM `%s`.`%s`.`%s`",
          PROJECT_ID, DATASET_NAME, TABLE_NAME);
  private static final MaterializedView mv2 =
      new MaterializedView(ATableId.of(PROJECT_ID, DATASET_NAME, TABLE_NAME), STATEMENT2);

  @InjectMocks private static BigQueryDatabaseFetcher fetcher;
  @Mock private static ResourceManager mockResourceManager;

  @BeforeClass
  public static void setup() {
    BigQueryOptions.Builder bqOptBuilder = BigQueryOptions.newBuilder();
    bqOptBuilder.setProjectId(PROJECT_ID);
    BigQuery bigQueryClient = bqOptBuilder.build().getService();
    fetcher = new BigQueryDatabaseFetcher(bigQueryClient, mockResourceManager);
    fetcher.createMaterializedView(mv1);
  }

  @AfterClass
  public static void cleanup() {
    fetcher.deleteMaterializedView(mv1);
    fetcher.deleteMaterializedView(mv2);
  }

  @Test
  public void basicTests() {
    fetcher.deleteMaterializedView(mv1);
    fetcher.createMaterializedView(mv1);
    fetcher.createMaterializedView(mv1);
    fetcher.deleteMaterializedView(mv1);
    fetcher.deleteMaterializedView(mv1);
  }

  @Test
  public void createMaterializedView__whenStatementInvalid_throwException() {
    assertThrows(BigQueryException.class, () -> fetcher.createMaterializedView(mv2));
  }

  @Test
  public void dryRun() {
    fetcher.dryRunQuery(STATEMENT1);
  }

  @Test
  public void dryRun__whenInvalidStatement_throwException() {
    assertThrows(BigQueryException.class, () -> fetcher.dryRunQuery(STATEMENT2));
  }
}
