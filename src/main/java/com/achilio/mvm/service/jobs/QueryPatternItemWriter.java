package com.achilio.mvm.service.jobs;


import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.repositories.QueryPatternRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class QueryPatternItemWriter implements ItemWriter<List<QueryPattern>> {

  private final QueryPatternRepository queryPatternRepository;

  public QueryPatternItemWriter(QueryPatternRepository queryPatternRepository) {
    this.queryPatternRepository = queryPatternRepository;
  }

  @Override
  public void write(List<? extends List<QueryPattern>> queryPatternLists) {
    for (List<QueryPattern> queryPatterns : queryPatternLists) {
      queryPatterns.forEach(this::save);
    }
  }

  @Transactional
  public void save(QueryPattern queryPattern) {
    queryPatternRepository.save(queryPattern);
  }
}
