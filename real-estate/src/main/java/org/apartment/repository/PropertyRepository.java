package org.apartment.repository;

import org.apartment.entity.Property;
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
}
