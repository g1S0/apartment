package org.apartment.repository;

import org.apartment.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyImageRepository
    extends JpaRepository<PropertyImage, String> {
  @Modifying
  @Query("DELETE FROM PropertyImage p WHERE p.property.postedBy = :userId")
  void deleteByUserId(@Param("userId") String userId);
}
