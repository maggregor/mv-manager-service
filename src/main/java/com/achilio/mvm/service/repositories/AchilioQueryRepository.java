package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.AchilioQuery;
import com.achilio.mvm.service.entities.FetcherQueryJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchilioQueryRepository extends JpaRepository<AchilioQuery, Long> {
  List<AchilioQuery> findAllByFetcherQueryJob_ProjectIdAndFetcherQueryJob(
      String projectId, FetcherQueryJob fetcherQueryJob);
}
