package org.apartment.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyImage;
import org.apartment.repository.PropertyRepository;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final UploadService uploadService;
  private final EntityManager entityManager;

  public PropertyService(PropertyRepository propertyRepository, UploadService uploadService,
                         EntityManager entityManager) {
    this.propertyRepository = propertyRepository;
    this.uploadService = uploadService;
    this.entityManager = entityManager;
  }

  @Transactional
  public Property createProperty(Property property, MultipartFile[] imageFiles) throws Exception {
    log.info("Starting the property creation process for property with title: {}",
        property.getTitle());

    try {
      log.info("Uploading images...");
      List<String> imageUrls = uploadService.uploadFiles(imageFiles);
      log.info("Images uploaded successfully. Number of images: {}", imageUrls.size());

      log.info("Mapping image URLs to PropertyImage entities...");
      List<PropertyImage> propertyImages = imageUrls.stream()
          .map(imageUrl -> PropertyImage.builder().imageUrl(imageUrl).property(property).build())
          .collect(Collectors.toList());
      log.info("Mapped {} image URLs to PropertyImage entities.", propertyImages.size());

      property.setImages(propertyImages);
      log.info("Property images set successfully.");

      Property savedProperty = propertyRepository.save(property);
      log.info("Property with ID {} created successfully.", savedProperty.getId());

      return savedProperty;
    } catch (Exception e) {
      log.error("Error occurred while creating property with title: {}", property.getTitle(), e);
      throw e;
    }
  }

  public List<Property> getAllProperties() {
    return propertyRepository.findAll();
  }

  public List<Property> searchProperties(String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                         LocalDateTime startDate, LocalDateTime endDate) {
    SearchSession searchSession = Search.session(entityManager);

    return searchSession.search(Property.class).where(
        f -> f.bool().must(f.match().fields("title", "description", "city").matching(keyword))
            .must(f.range().field("price").between(minPrice, maxPrice))
            .must(f.range().field("createdAt").between(startDate, endDate))).fetchHits(100);
  }
}
