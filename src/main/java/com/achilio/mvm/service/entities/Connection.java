package com.achilio.mvm.service.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "connections")
@AllArgsConstructor
@NoArgsConstructor
@EnableJpaAuditing
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Connection {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private String id;

  @Column private String name;

  @Column private String teamId;

  public abstract ConnectionType getType();

  public enum ConnectionType {
    SERVICE_ACCOUNT;
  }
}
