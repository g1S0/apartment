package org.apartment.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyImage;
import org.apartment.mapper.PropertyMapper;
import org.apartment.repository.PropertyImageRepository;
import org.apartment.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final UploadService uploadService;
  private final PropertyMapper propertyMapper;
  private final TransactionTemplate transactionTemplate;

  @Transactional
  public Property createProperty(Property property, MultipartFile[] imageFiles) {
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

  @KafkaListener(topics = "account_data_delete", groupId = "clear_user_data")
  public void deleteProperty(String userId) {
    List<String> imageUrls = transactionTemplate.execute(status -> {
      List<String> urls = propertyImageRepository.findImageUrlsByUserId(userId);
      propertyImageRepository.deleteByUserId(userId);
      propertyRepository.deleteByUserId(userId);
      return urls;
    });

    uploadService.deleteFiles(imageUrls);
  }

  public Page<PropertyDto> getProperties(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Property> propertyPage = propertyRepository.findAll(pageable);

    return propertyPage.map(propertyMapper::toDto);
  }
}
