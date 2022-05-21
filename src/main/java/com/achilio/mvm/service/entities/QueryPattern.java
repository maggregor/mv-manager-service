package com.achilio.mvm.service.entities;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column
  private String projectId;

  @Column
  private String datasetName;

  @Column
  private String tableRefId;

  @Column
  private Long hitCount;

  @ElementCollection
  private Set<TableRef> tables;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "field_sets",
      joinColumns = @JoinColumn(name = "query_pattern_id"),
      inverseJoinColumns = @JoinColumn(name = "field_id"))
  private Set<Field> fields = new HashSet<>();

  public QueryPattern(String projectId, String datasetName, String tableRefId) {
    this.projectId = projectId;
    this.datasetName = datasetName;
    this.tableRefId = tableRefId;
  }

  public void addField(Field field) {
    if (field.getTableRefId().equals(this.tableRefId)) {
      this.fields.add(field);
    }
  }
}
