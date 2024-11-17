package org.apartment.service;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apartment.entity.Property;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PropertySearchService {

  private final EntityManager entityManager;

  public PropertySearchService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public List<Property> searchProperties(String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                         LocalDateTime startDate, LocalDateTime endDate) {
    log.info(
        "Starting search for properties with keyword: {}, price range: {} - {}, date range: {} - {}",
        keyword, minPrice, maxPrice, startDate, endDate);

    SearchSession searchSession = Search.session(entityManager);

    return searchSession.search(Property.class).where(
        f -> f.bool().must(f.match().fields("title", "description", "city").matching(keyword))
            .must(f.range().field("price").between(minPrice, maxPrice))
            .must(f.range().field("createdAt").between(startDate, endDate))).fetchHits(100);
  }
}
