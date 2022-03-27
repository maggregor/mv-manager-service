package com.achilio.mvm.service.services;

import com.achilio.mvm.service.repositories.AOrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Services to manage connection resources. */
@Service
public class AConnectionService {

  @Autowired private AOrganizationRepository organizationRepository;
}
