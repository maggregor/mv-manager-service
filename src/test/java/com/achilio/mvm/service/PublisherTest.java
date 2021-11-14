package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.services.GooglePublisherService;
import java.util.ArrayList;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PublisherTest {

  @Test
  @Ignore
  public void testPublisher() {
    Optimization optimization = new Optimization("achilio-dev");
    GooglePublisherService publisherService = new GooglePublisherService();
    publisherService.publishOptimization(optimization, new ArrayList<>());
  }
}
