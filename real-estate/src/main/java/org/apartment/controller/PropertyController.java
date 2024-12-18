package org.apartment.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apartment.dto.PropertyDto;
import org.apartment.dto.PropertyListDto;
import org.apartment.entity.Property;
import org.apartment.mapper.PropertyMapper;
import org.apartment.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/property")
@AllArgsConstructor
public class PropertyController {
  private PropertyService propertyService;

  @PostMapping
  public ResponseEntity<Property> createProperty(
      @RequestPart("data") @Valid PropertyDto propertyDto,
      @RequestParam("image") MultipartFile[] imageFiles,
      @RequestHeader("X-User-Id") String userId) {
    propertyDto.setPostedBy(userId);
    Property property = PropertyMapper.INSTANCE.toEntity(propertyDto);
    Property createdProperty = propertyService.createProperty(property, imageFiles);
    return ResponseEntity.ok(createdProperty);
  }

  @GetMapping
  public PropertyListDto getProperties(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10")
                                       int size) {
    return propertyService.getProperties(page, size);
  }
}
