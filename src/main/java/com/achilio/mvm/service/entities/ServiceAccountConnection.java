package com.achilio.mvm.service.entities;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@DiscriminatorValue("service_account")
public class ServiceAccountConnection extends Connection {

  @Column
  @NotEmpty(message = "Service account must not be empty")
  private String serviceAccount;

  @VisibleForTesting
  public ServiceAccountConnection(String id, String teamId, String serviceAccount) {
    super.setId(id);
    super.setTeamId(teamId);
    this.setServiceAccount(serviceAccount);
  }

  @Override
  public ConnectionType getType() {
    return ConnectionType.SERVICE_ACCOUNT;
  }
}
