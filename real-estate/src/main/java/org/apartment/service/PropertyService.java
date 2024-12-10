package org.apartment.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyImage;
import org.apartment.mapper.PropertyMapper;
import org.apartment.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final UploadService uploadService;
  private final PropertyMapper propertyMapper;

  public PropertyService(PropertyRepository propertyRepository, UploadService uploadService,
                         PropertyMapper propertyMapper) {
    this.propertyRepository = propertyRepository;
    this.uploadService = uploadService;
    this.propertyMapper = propertyMapper;
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


  public Page<PropertyDto> getProperties(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Property> propertyPage = propertyRepository.findAll(pageable);

    return propertyPage.map(propertyMapper::toDto);
  }
}
