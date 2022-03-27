package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "connection")
public class AConnection {

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column private String serviceAccount;


}
