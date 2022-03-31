package com.achilio.mvm.service.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;
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
  private Long id;

  @Column private String name;

  @Column private String teamName;

  @Column private String ownerUsername;

  @Column
  @Enumerated(EnumType.STRING)
  private SourceType sourceType;

  @UpdateTimestamp @Column private LocalDateTime lastModifiedAt;

  public abstract ConnectionType getType();

  public abstract String getContent();

  public abstract void setContent(String content);

  public void setName(String name) {
    if (name != null) {
      this.name = name;
    }
  }

  public enum ConnectionType {
    SERVICE_ACCOUNT
  }

  public enum SourceType {
    BIGQUERY
  }
}
