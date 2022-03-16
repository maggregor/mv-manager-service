package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
public class AOrganization {

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column private String name;

  @Column(nullable = false)
  private String stripeCustomerId;

  @Column private String googleWorkspaceId;

  public AOrganization() {}

  public AOrganization(String id, String name, String stripeCustomerId, String googleWorkspaceId) {
    this.id = id;
    this.name = name;
    this.stripeCustomerId = stripeCustomerId;
    this.googleWorkspaceId = googleWorkspaceId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStripeCustomerId() {
    return stripeCustomerId;
  }

  public void setStripeCustomerId(String stripeCustomerId) {
    this.stripeCustomerId = stripeCustomerId;
  }

  public String getGoogleWorkspaceId() {
    return googleWorkspaceId;
  }

  public void setGoogleWorkspaceId(String googleWorkspaceId) {
    this.googleWorkspaceId = googleWorkspaceId;
  }
}
