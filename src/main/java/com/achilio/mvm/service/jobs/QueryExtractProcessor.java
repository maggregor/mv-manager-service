package com.achilio.mvm.service.jobs;

import com.achilio.mvm.service.entities.AQuery;
import com.achilio.mvm.service.entities.ATable;
import com.achilio.mvm.service.entities.QueryPattern;
import com.achilio.mvm.service.services.ProjectService;
import com.achilio.mvm.service.visitors.NewZetaSQLExtract;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class QueryExtractProcessor implements ItemProcessor<AQuery, List<QueryPattern>> {

  private final ProjectService projectService;

  public QueryExtractProcessor(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public List<QueryPattern> process(@NonNull AQuery query) {
    Set<ATable> tables = new HashSet<>(projectService.getAllTables("achilio-dev"));
    NewZetaSQLExtract newZetaSQLExtract = new NewZetaSQLExtract(tables);
    return newZetaSQLExtract.extractAll(query);
  }

}
