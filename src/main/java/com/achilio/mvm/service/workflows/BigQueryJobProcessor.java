package com.achilio.mvm.service.workflows;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.BigQueryJob;
import com.achilio.mvm.service.visitors.ATableId;
import com.achilio.mvm.service.visitors.ZetaSQLExtract;
import com.google.cloud.bigquery.Job;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class BigQueryJobProcessor implements ItemProcessor<Job, AQuery> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQueryJobProcessor.class);

  private final ZetaSQLExtract extractor = new ZetaSQLExtract();

  @Override
  public AQuery process(@NonNull Job job) {
    try {
      BigQueryJob queryJob = new BigQueryJob(job);
      try {
        List<ATableId> aTableIds = extractor.extractATableIds(queryJob);
        queryJob.setQueryTableId(
            aTableIds.stream().map(ATableId::asPath).collect(Collectors.toList()));
      } catch (Exception e) {
        LOGGER.debug("Cannot find ATableId for the statement {}", queryJob.getQuery());
      }
      return queryJob;
    } catch (Exception ignored) {
    }
    return null;
  }
}
