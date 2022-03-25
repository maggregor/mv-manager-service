package com.achilio.mvm.service.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @Column
  private OrganizationType organizationType;

  public AOrganization() {}

  public AOrganization(
      String id, String name, String stripeCustomerId, OrganizationType organizationType) {
    this(id, name, stripeCustomerId, organizationType, null);
  }

  public AOrganization(
      String id,
      String name,
      String stripeCustomerId,
      OrganizationType organizationType,
      String googleWorkspaceId) {
    this.id = id;
    this.name = name;
    this.stripeCustomerId = stripeCustomerId;
    this.organizationType = organizationType;
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

  public OrganizationType getOrganizationType() {
    return organizationType;
  }

  public String getGoogleWorkspaceId() {
    return googleWorkspaceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AOrganization that = (AOrganization) o;

    if (!id.equals(that.id)) {
      return false;
    }
    if (!Objects.equals(name, that.name)) {
      return false;
    }
    if (!Objects.equals(stripeCustomerId, that.stripeCustomerId)) {
      return false;
    }
    if (!Objects.equals(googleWorkspaceId, that.googleWorkspaceId)) {
      return false;
    }
    return organizationType == that.organizationType;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (stripeCustomerId != null ? stripeCustomerId.hashCode() : 0);
    result = 31 * result + (googleWorkspaceId != null ? googleWorkspaceId.hashCode() : 0);
    result = 31 * result + (organizationType != null ? organizationType.hashCode() : 0);
    return result;
  }

  public enum OrganizationType {
    ORGANIZATION,
    NO_ORGANIZATION
  }
}
