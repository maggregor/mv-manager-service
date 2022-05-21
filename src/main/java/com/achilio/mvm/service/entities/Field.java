package com.achilio.mvm.service.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@EnableJpaAuditing
@Table(name = "field")
public class Field {

  private static final String ALIAS_PREFIX = "a_";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING)
  private FieldType fieldType;

  @Column
  private String expression;

  @Column
  private String projectId;

  public Field(FieldType fieldType, String expression) {
    this.fieldType = fieldType;
    this.expression = expression;
  }

  protected Field() {
  }

  public String getAlias() {
    return ALIAS_PREFIX + Math.abs(expression.hashCode());
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Field)) {
      return false;
    }

    Field field = (Field) o;

    if (fieldType != field.fieldType) {
      return false;
    }
    return Objects.equals(expression, field.expression);
  }

  @Override
  public int hashCode() {
    int result = fieldType != null ? fieldType.hashCode() : 0;
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    return result;
  }

  public enum FieldType {
    REFERENCE,
    AGGREGATE,
    FUNCTION
  }
}
