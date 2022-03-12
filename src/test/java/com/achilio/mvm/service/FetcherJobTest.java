package com.achilio.mvm.service;

import static org.junit.Assert.assertEquals;

import com.achilio.mvm.service.entities.FetcherQueryJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetcherJobTest {

  private final String TEST_PROJECT_ID = "myProjectId";
  private final Long TIMEFRAME = 7L;

  @Test
  public void simpleValidation() {
    FetcherQueryJob fetcherJob = new FetcherQueryJob(TEST_PROJECT_ID);
    assertEquals(TEST_PROJECT_ID, fetcherJob.getProjectId());
    assertEquals(TIMEFRAME, fetcherJob.getTimeframe());
  }
}