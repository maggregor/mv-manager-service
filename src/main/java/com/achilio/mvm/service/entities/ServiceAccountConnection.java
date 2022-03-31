package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("service_account")
public class ServiceAccountConnection extends Connection {

  @Column(name = "content", length = 65535)
  @NotEmpty(message = "Service account must not be empty")
  private String serviceAccountKey;

  public ServiceAccountConnection(
      String name,
      String teamName,
      String ownerUsername,
      SourceType sourceType,
      String serviceAccountKey) {
    this.setName(name);
    this.setTeamName(teamName);
    this.setOwnerUsername(ownerUsername);
    this.setSourceType(sourceType);
    this.setServiceAccountKey(serviceAccountKey);
  }

  @Override
  public ConnectionType getType() {
    return ConnectionType.SERVICE_ACCOUNT;
  }

  @Override
  public String getContent() {
    return this.serviceAccountKey;
  }

  @Override
  public void setContent(String content) {
    if (content != null) {
      this.serviceAccountKey = content;
    }
  }

  public void setServiceAccountKey(String serviceAccountKey) {
    if (serviceAccountKey != null) {
      this.serviceAccountKey = serviceAccountKey;
    }
  }
}
