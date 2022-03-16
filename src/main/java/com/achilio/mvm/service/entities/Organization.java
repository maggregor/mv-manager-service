package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
public class Organization {

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column private String name;

  @Column(nullable = false)
  private String stripeCustomerId;

  public Organization() {}

  public Organization(Long id, String name, String stripeCustomerId) {
    this.id = id;
    this.name = name;
    this.stripeCustomerId = stripeCustomerId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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
}
