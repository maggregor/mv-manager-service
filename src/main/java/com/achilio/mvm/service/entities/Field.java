package com.achilio.mvm.service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@NoArgsConstructor
@EnableJpaAuditing
@Table(name = "field")
public class Field {

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column
  @Enumerated(EnumType.STRING)
  private FieldType fieldType;

  @Column
  private String projectId;

  @Column private String colName;

  @Column private String tableRefId;

  @ManyToMany(mappedBy = "fields", cascade = CascadeType.ALL)
  @JsonIgnore // Ignore queryPatterns to avoid infinite recursion in responses
  private Set<QueryPattern> queryPatterns;

  public Field(FieldType fieldType, String colName, String tableRefId, String projectId) {
    this.fieldType = fieldType;
    this.colName = colName;
    this.tableRefId = tableRefId;
    this.projectId = projectId;
    this.setId();
  }

  public void setId() {
    this.id = String.format("%s#%s:%s", this.tableRefId, this.fieldType, this.colName);
  }

  public enum FieldType {
    REFERENCE,
    AGGREGATE,
    FUNCTION
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Field field = (Field) o;

    if (!id.equals(field.id)) {
      return false;
    }
    if (fieldType != field.fieldType) {
      return false;
    }
    if (!colName.equals(field.colName)) {
      return false;
    }
    return tableRefId.equals(field.tableRefId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + fieldType.hashCode();
    result = 31 * result + colName.hashCode();
    result = 31 * result + tableRefId.hashCode();
    return result;
  }
}
