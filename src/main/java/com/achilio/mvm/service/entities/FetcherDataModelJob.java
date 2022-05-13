package com.achilio.mvm.service.entities;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class FetcherDataModelJob extends Job {

  public FetcherDataModelJob() {
  }

  public FetcherDataModelJob(String projectId) {
    super(projectId);
  }
}
