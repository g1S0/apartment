package org.apartment.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.PropertySearchDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PropertySearchService {

  private final EntityManager entityManager;

  public List<Property> searchProperties(PropertySearchDto propertySearchDto, int page, int size) {
    log.info("Starting search for properties with keyword: {}, price range: {} - {}, "
            + "date range: {} - {}, property type: {}, city: {}, deal type: {}",
        propertySearchDto.getKeyword(), propertySearchDto.getMinPrice(),
        propertySearchDto.getMaxPrice(), propertySearchDto.getStartDate(),
        propertySearchDto.getEndDate(), propertySearchDto.getPropertyType(),
        propertySearchDto.getCity(), propertySearchDto.getPropertyDealType());

    long startTime = System.currentTimeMillis();
    log.info("Search started at: {}", startTime);

    SearchSession searchSession = Search.session(entityManager);

    List<Property> properties = searchSession.search(Property.class).where(f -> {
      BooleanPredicateClausesStep<?> query = f.bool().must(f.range().field("price")
              .between(propertySearchDto.getMinPrice(), propertySearchDto.getMaxPrice()))
          .must(f.range().field("createdAt")
              .between(propertySearchDto.getStartDate(), propertySearchDto.getEndDate()));

      if (!propertySearchDto.getKeyword().isEmpty()) {
        query.must(f.match().fields("title", "description").matching(propertySearchDto.getKeyword()));
      }

      if (!propertySearchDto.getCity().isEmpty()) {
        query.must(f.match().fields("city").matching(propertySearchDto.getCity()));
      }

      if (propertySearchDto.getStatus() != null && !propertySearchDto.getStatus().isEmpty()) {
        query.must(f.match().fields("status")
            .matching(PropertyStatus.valueOf(propertySearchDto.getStatus().toUpperCase())));
      }

      if (propertySearchDto.getPropertyType() != null) {
        query.must(f.match().field("type").matching(propertySearchDto.getPropertyType()));
      }

      if (propertySearchDto.getPropertyDealType() != null) {
        query.must(f.match().field("propertyDealType").matching(propertySearchDto.getPropertyDealType()));
      }

      return query;
    }).fetch(page * size, size).hits();

    long endTime = System.currentTimeMillis();
    log.info("Search completed at: {}, Total time: {} ms", endTime, endTime - startTime);

    return properties;
  }
}
