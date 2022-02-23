package com.achilio.mvm.service.services;

import com.achilio.mvm.service.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StripeService {
  @Autowired
  private ProjectRepository projectRepository;

  public void handleSubscriptionCreated(String customerId) {

  }
}
