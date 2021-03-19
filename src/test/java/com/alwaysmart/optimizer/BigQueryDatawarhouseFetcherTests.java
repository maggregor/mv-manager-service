package com.alwaysmart.optimizer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

// public class BigQueryDatawarhouseFetcherTests {
//     @Test
//     public void testFetchQueriesNoDate1() {
//         BigQueryDatabaseFetcher fetcher = new BigQueryDatabaseFetcher();
//         List<FetchedQuery> actual = fetcher.fetchQueries(null, null, null);
//         System.out.println(actual.get(0).statement());
//         System.out.println(actual.get(0).cost());
//     }

//     @Test
//     public void testFetchQueriesDate1() {
//         BigQueryDatabaseFetcher fetcher = new BigQueryDatabaseFetcher();
//         SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
//         formatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
//         String dateInString = "02-02-2021 10:15:55 AM";
//         Date date;
//         try {
//             date = formatter.parse(dateInString);
//         } catch (ParseException e) {
//             date = new Date();
//             e.printStackTrace();
//         }
//         List<FetchedQuery> actual = fetcher.fetchQueries(null, null, date);
//         System.out.println(actual.get(0).statement());
//         System.out.println(actual.get(0).cost());
//     }

//     @Test
//     public void testFetchTableMetadata() {
//         String datasetName = "integration";
//         String tableName = "optimizer1";
//         BigQueryDatabaseFetcher fetcher = new BigQueryDatabaseFetcher();
//         TableMetadata tableMetadata = fetcher.fetchTableMetadata(datasetName, tableName);
//         System.out.println("SchemaName: " + tableMetadata.schema());
//         System.out.println("TableName: " + tableMetadata.table());
//         System.out.println("Columns: " + tableMetadata.columns());
//     }
// }
