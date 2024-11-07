package org.apartment.service;

import jakarta.transaction.Transactional;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyImage;
import org.apartment.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UploadService uploadService;

    public PropertyService(PropertyRepository propertyRepository, UploadService uploadService) {
        this.propertyRepository = propertyRepository;
        this.uploadService = uploadService;
    }

    @Transactional
    public Property createProperty(Property property, MultipartFile[] imageFiles) throws Exception {
        List<String> imageUrls = uploadService.uploadFiles(imageFiles);

        List<PropertyImage> propertyImages = imageUrls.stream()
                .map(imageUrl -> PropertyImage
                        .builder()
                        .imageUrl(imageUrl)
                        .property(property)
                        .build())
                .collect(Collectors.toList());

        property.setImages(propertyImages);

        return propertyRepository.save(property);
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }
}
