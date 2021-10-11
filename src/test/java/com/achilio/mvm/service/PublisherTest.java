package com.achilio.mvm.service;

import com.achilio.mvm.service.entities.Optimization;
import com.achilio.mvm.service.services.GooglePublisherService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
public class PublisherTest {

    @Test @Ignore
    public void testPublisher() throws IOException, ExecutionException, InterruptedException {
        Optimization optimization = new Optimization("achilio-dev", "europe-west1", "nyc_trips", false);
        GooglePublisherService publisherService = new GooglePublisherService();
        publisherService.publishOptimization(optimization, new ArrayList<>(), "");
    }
}
