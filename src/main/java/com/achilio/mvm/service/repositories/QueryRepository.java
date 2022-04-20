package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.AQuery;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends JpaRepository<AQuery, String> {

  Optional<AQuery> findQueryByProjectIdAndId(String projectId, String queryId);

  List<AQuery> findAllByProjectId(String projectId);

  List<AQuery> findAllByProjectIdAndStartTimeGreaterThanEqual(String projectId, Date date);

  /**
   * Statistics are excluding cached queries
   **/
  @Query("SELECT AVG(q.processedBytes) FROM AQuery q WHERE q.projectId = :projectId AND q.startTime > :date AND q.useCache = false")
  Long averageProcessedBytesByProjectAndStartTimeGreaterThanEqual(
      @Param("projectId") String projectId, @Param("date") Date date);

  @Query("SELECT SUM(1) FROM AQuery q WHERE q.projectId = :projectId AND q.startTime > :date AND q.useCache = false")
  Long countQueryByProjectAndStartTimeGreaterThanEqual(
      @Param("projectId") String projectId, @Param("date") Date date);

  @Query("SELECT SUM(1) FROM AQuery q WHERE q.projectId = :projectId AND q.startTime > :date AND q.useCache = false AND q.useMaterializedView = true")
  Long countQueryInMVByProjectAndStartTimeGreaterThanEqual(
      @Param("projectId") String projectId, @Param("date") Date date);
}
