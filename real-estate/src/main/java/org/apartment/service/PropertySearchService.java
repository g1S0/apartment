package org.apartment.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.PropertySearchDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
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

  public List<Property> searchProperties(PropertySearchDto propertySearchDto) {
    log.info("Starting search for properties with keyword: {}, price range: {} - {}, "
            + "date range: {} - {}, property type: {}, city: {}", propertySearchDto.getKeyword(),
        propertySearchDto.getMinPrice(), propertySearchDto.getMaxPrice(),
        propertySearchDto.getStartDate(), propertySearchDto.getEndDate(),
        propertySearchDto.getPropertyType(), propertySearchDto.getCity());

    SearchSession searchSession = Search.session(entityManager);

    return searchSession.search(Property.class).where(f -> {
      BooleanPredicateClausesStep<?> query = f.bool().must(f.range().field("price")
          .between(propertySearchDto.getMinPrice(), propertySearchDto.getMaxPrice())).must(
          f.range().field("createdAt")
              .between(propertySearchDto.getStartDate(), propertySearchDto.getEndDate()));

      if (!propertySearchDto.getKeyword().isEmpty()) {
        query.must(
            f.match().fields("title", "description").matching(propertySearchDto.getKeyword()));
      }

      if (!propertySearchDto.getCity().isEmpty()) {
        query.must(f.match().fields("city").matching(propertySearchDto.getCity()));
      }

      if (!propertySearchDto.getStatus().isEmpty()) {
        query.must(f.match().fields("status")
            .matching(PropertyStatus.valueOf(propertySearchDto.getStatus().toUpperCase())));
      }

      if (propertySearchDto.getPropertyType() != null) {
        query.must(f.match().field("type").matching(propertySearchDto.getPropertyType()));
      }

      return query;
    }).fetchHits(100);
  }
}
