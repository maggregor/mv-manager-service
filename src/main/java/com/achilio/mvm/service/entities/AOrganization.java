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

  public String getName() {
    return name;
  }

  public String getStripeCustomerId() {
    return stripeCustomerId;
  }

  public String getGoogleWorkspaceId() {
    return googleWorkspaceId;
  }
}
