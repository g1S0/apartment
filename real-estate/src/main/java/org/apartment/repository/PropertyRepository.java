package org.apartment.repository;

import java.util.List;
import java.util.Set;
import org.apartment.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository
    extends JpaRepository<Property, String>, PagingAndSortingRepository<Property, String> {
  @Modifying
  @Query("DELETE FROM Property p WHERE p.postedBy = :userId")
  void deleteByUserId(@Param("userId") String userId);

  @EntityGraph(value = "property_entity-graph")
  List<Property> findByIdIn(Set<String> ids);

  @Query("SELECT p.id FROM Property p")
  Page<String> findAllIds(Pageable pageable);
}
