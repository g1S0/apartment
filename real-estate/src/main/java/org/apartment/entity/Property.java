package org.apartment.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "property")
@Indexed
@NamedEntityGraph(name = "property_entity-graph", attributeNodes = @NamedAttributeNode("images"))
public class Property {
  @Id
  @UuidGenerator
  private String id;

  @Column(nullable = false)
  @FullTextField
  private String title;

  @Column(length = 2000)
  @FullTextField
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @GenericField
  private PropertyType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @GenericField
  private PropertyDealType propertyDealType;

  @Column(nullable = false)
  @GenericField
  private BigDecimal price;

  @Column(nullable = false)
  @FullTextField
  private String city;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @GenericField
  private PropertyStatus status;

  @Column(name = "posted_by", nullable = false)
  private String postedBy;

  @Column(name = "created_at", updatable = false)
  @GenericField
  private LocalDate createdAt;

  @Column(name = "updated_at")
  private LocalDate updatedAt;

  @JsonManagedReference
  @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<PropertyImage> images;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDate.now();
    updatedAt = LocalDate.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDate.now();
  }
}
