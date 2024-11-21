package org.apartment.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apartment.dto.PropertyDto;
import org.apartment.dto.PropertySearchDto;
import org.apartment.entity.Property;
import org.apartment.mapper.PropertyMapper;
import org.apartment.service.PropertySearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/property/search")
@AllArgsConstructor
public class PropertySearchController {

  private final PropertySearchService propertySearchService;

  @GetMapping
  public ResponseEntity<List<PropertyDto>> searchProperties(
      @RequestBody PropertySearchDto propertySearchDto) {
    List<Property> properties = propertySearchService.searchProperties(propertySearchDto);
    List<PropertyDto> propertyDtoList =
        properties.stream().map(PropertyMapper.INSTANCE::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(propertyDtoList);
  }
}
