package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.Connection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, String> {

  Optional<Connection> findByIdAndTeamName(Long id, String teamName);

  void deleteByIdAndTeamName(Long id, String teamName);

  List<Connection> findAllByTeamName(String teamName);
}
