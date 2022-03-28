package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Connection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<Connection, String> {

  Optional<Connection> findByIdAndTeamId(String id, String teamId);

  void deleteByIdAndTeamId(String id, String teamId);

  List<Connection> findAllByTeamId(String teamId);
}
