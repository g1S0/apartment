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

  public List<Property> searchProperties(String keyword, String city, String status,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         LocalDate startDate, LocalDate endDate,
                                         PropertyType propertyType) {
    final String effectiveKeyword = (keyword != null) ? keyword : "";
    final String effectiveCity = (city != null) ? city : "";
    final String effectiveStatus = (status != null) ? status : "";
    final BigDecimal effectiveMinPrice = (minPrice != null) ? minPrice : BigDecimal.ZERO;
    final BigDecimal effectiveMaxPrice =
        (maxPrice != null) ? maxPrice : new BigDecimal("1000000000.0");
    final LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.MIN;
    final LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.MAX;

    log.info("Starting search for properties with keyword: {}, price range: {} - {}, "
            + "date range: {} - {}, property type: {}, city: {}", effectiveKeyword, effectiveMinPrice,
        effectiveMaxPrice, effectiveStartDate, effectiveEndDate, propertyType, city);

    SearchSession searchSession = Search.session(entityManager);

    return searchSession.search(Property.class).where(f -> {
      BooleanPredicateClausesStep<?> query =
          f.bool().must(f.range().field("price").between(effectiveMinPrice, effectiveMaxPrice))
              .must(f.range().field("createdAt").between(effectiveStartDate, effectiveEndDate));

      if (!effectiveKeyword.isBlank()) {
        query.must(f.match().fields("title", "description").matching(effectiveKeyword));
      }

      if (!effectiveCity.isBlank()) {
        query.must(f.match().fields("city").matching(effectiveCity));
      }

      if (!effectiveStatus.isBlank()) {
        query.must(f.match().fields("status").matching(effectiveStatus));
      }

      if (propertyType != null) {
        query.must(f.match().field("type").matching(propertyType));
      }

      return query;
    }).fetchHits(100);
  }
}
