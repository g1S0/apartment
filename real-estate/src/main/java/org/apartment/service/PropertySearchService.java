package org.apartment.service;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyType;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
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
                                         LocalDate startDate, LocalDate endDate,
                                         PropertyType propertyType) {

    final String effectiveKeyword = (keyword != null) ? keyword : "";
    final BigDecimal effectiveMinPrice = (minPrice != null) ? minPrice : BigDecimal.ZERO;
    final BigDecimal effectiveMaxPrice =
        (maxPrice != null) ? maxPrice : new BigDecimal("1000000000.0");
    final LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.MIN;
    final LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.MAX;

    log.info("Starting search for properties with keyword: {}, price range: {} - {}, "
            + "date range: {} - {}, property type: {}", effectiveKeyword, effectiveMinPrice,
        effectiveMaxPrice, effectiveStartDate, effectiveEndDate, propertyType);

    SearchSession searchSession = Search.session(entityManager);

    return searchSession.search(Property.class).where(f -> {
      BooleanPredicateClausesStep<?> query =
          f.bool().must(f.match().fields("title", "description", "city").matching(effectiveKeyword))
              .must(f.range().field("price").between(effectiveMinPrice, effectiveMaxPrice))
              .must(f.range().field("createdAt").between(effectiveStartDate, effectiveEndDate));

      if (propertyType != null) {
        query.must(f.match().field("type").matching(propertyType));
      }

      return query;
    }).fetchHits(100);
  }
}
