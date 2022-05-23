package com.achilio.mvm.service.entities;

import com.achilio.mvm.service.entities.Field.FieldType;
import com.achilio.mvm.service.entities.TableRef.TableRefType;
import com.achilio.mvm.service.visitors.fieldsets.FieldSetIneligibilityReason;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Getter
@EnableJpaAuditing
@NoArgsConstructor
@Table(name = "query_pattern")
public class QueryPattern {

  @ElementCollection private final Set<TableRef> tables = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "field_sets",
      joinColumns = @JoinColumn(name = "query_pattern_id"),
      inverseJoinColumns = @JoinColumn(name = "field_id"))
  private final Set<Field> fields = new HashSet<>();

  @ElementCollection
  @CollectionTable(
      name = "query_pattern_ineligibility",
      joinColumns = @JoinColumn(name = "query_pattern_id"))
  @Column
  private final Set<FieldSetIneligibilityReason> ineligibilityReasons = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column private String projectId;
  @Column private Integer hitCount = 0;

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public void add(Field field) {
    this.hitCount++;
    this.fields.add(field);
  }

  public void merge(QueryPattern pattern) {
    this.tables.addAll(pattern.getTables());
    this.addAll(pattern.getFields());
  }

  public void addTableRef(TableRef tableRef) {
    this.tables.add(tableRef);
  }

  private void addAll(Set<Field> fields) {
    fields.forEach(this::add);
  }

  public void addIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {
    ineligibilityReasons.add(ineligibilityReason);
  }

  public void removeIneligibilityReason(FieldSetIneligibilityReason ineligibilityReason) {
    ineligibilityReasons.remove(ineligibilityReason);
  }

  public void clearIneligibilityReasons() {
    ineligibilityReasons.clear();
  }

  public Set<FieldSetIneligibilityReason> getIneligibilityReasons() {
    return Collections.unmodifiableSet(ineligibilityReasons);
  }

  public Set<Field> aggregates() {
    return this.fields.stream()
        .filter(field -> field.getFieldType().equals(FieldType.AGGREGATE))
        .collect(Collectors.toSet());
  }

  public Set<Field> references() {
    return this.fields.stream()
        .filter(field -> field.getFieldType().equals(FieldType.REFERENCE))
        .collect(Collectors.toSet());
  }

  public Set<Field> functions() {
    return this.fields.stream()
        .filter(field -> field.getFieldType().equals(FieldType.FUNCTION))
        .filter(field -> !aggregates().contains(field))
        .collect(Collectors.toSet());
  }

  public TableRef getMainTable() {
    return this.tables.stream()
        .filter(t -> t.getOrigin().equals(TableRefType.MAIN))
        .findFirst()
        .orElse(null);
  }

  public boolean isEmpty() {
    return this.fields.isEmpty();
  }

  public boolean isEligible() {
    return ineligibilityReasons.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QueryPattern)) {
      return false;
    }

    QueryPattern that = (QueryPattern) o;

    if (!tables.equals(that.tables)) {
      return false;
    }
    if (!fields.equals(that.fields)) {
      return false;
    }
    return Objects.equals(projectId, that.projectId);
  }

  @Override
  public int hashCode() {
    int result = tables.hashCode();
    result = 31 * result + fields.hashCode();
    result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
    return result;
  }
}
