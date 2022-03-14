package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import com.achilio.mvm.service.entities.Query;
import com.achilio.mvm.service.entities.statistics.QueryUsageStatistics;
import com.achilio.mvm.service.repositories.FetcherJobRepository;
import com.achilio.mvm.service.repositories.QueryRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryRepositoryTest {

  private static boolean setUpIsDone = false;
  private final String TEST_PROJECT_ID1 = "queryRepositoryProjectId";
  private final String TEST_PROJECT_ID2 = "myOtherQueryRepositoryProjectId";
  private final String table1 = "myTable1";
  private final String table2 = "myTable2";
  private final Set<String> refTables = new HashSet<>(Arrays.asList(table1, table1, table2));
  private final QueryUsageStatistics stats = new QueryUsageStatistics(1, 10L, 100L);
  private final FetcherQueryJob job1 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final FetcherQueryJob job2 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final FetcherQueryJob job3 = new FetcherQueryJob(TEST_PROJECT_ID1);
  private final FetcherQueryJob job4 = new FetcherQueryJob(TEST_PROJECT_ID2);

  @Autowired QueryRepository queryRepository;
  @Autowired FetcherJobRepository fetcherJobRepository;

  @Before
  public void setup() {
    if (setUpIsDone) {
      return;
    }
    fetcherJobRepository.save(job1);
    fetcherJobRepository.save(job2);
    fetcherJobRepository.save(job3);
    fetcherJobRepository.save(job4);
    Query query1 =
        new Query(job1, "SELECT 1", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query2 =
        new Query(job1, "SELECT 2", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query3 =
        new Query(job2, "SELECT 2", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    Query query4 =
        new Query(job4, "SELECT 1", false, false, LocalDate.of(2020, 1, 8), refTables, stats);
    queryRepository.save(query1);
    queryRepository.save(query2);
    queryRepository.save(query3);
    queryRepository.save(query4);
    setUpIsDone = true;
  }

  @Test
  public void findAllByFetcherQueryJobTest() {
    List<Query> queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job1, TEST_PROJECT_ID1);
    Assert.assertEquals(2, queries.size());

    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job2, TEST_PROJECT_ID1);
    Assert.assertEquals(1, queries.size());

    // Job has no query
    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job3, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    // Job has queries but projectId doesn't match
    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job4, TEST_PROJECT_ID1);
    Assert.assertEquals(0, queries.size());

    queries =
        queryRepository.findAllByFetcherQueryJobAndFetcherQueryJob_ProjectId(
            job4, TEST_PROJECT_ID2);
    Assert.assertEquals(1, queries.size());
  }
}
