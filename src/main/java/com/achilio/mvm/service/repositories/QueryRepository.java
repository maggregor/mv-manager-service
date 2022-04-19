package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Query;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends JpaRepository<Query, String> {

  Optional<Query> findQueryByProjectIdAndId(String projectId, String queryId);

  List<Query> findAllByProjectId(String projectId);

  List<Query> findAllByProjectIdAndStartTimeGreaterThanEqual(String projectId, Date date);
}
