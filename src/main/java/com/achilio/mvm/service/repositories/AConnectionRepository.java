package com.achilio.mvm.service.repositories;

import com.achilio.mvm.service.entities.AOrganization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AConnectionRepository extends JpaRepository<AOrganization, String> {

  Optional<AOrganization> findAOrganizationById(String organizationId);
}
