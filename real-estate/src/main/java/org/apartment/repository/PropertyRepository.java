package org.apartment.repository;

import org.apartment.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository
    extends JpaRepository<Property, String>, PagingAndSortingRepository<Property, String> {
}
