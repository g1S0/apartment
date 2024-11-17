package org.apartment.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyType;
import org.apartment.mapper.PropertyMapper;
import org.apartment.service.PropertySearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/property/search")
@AllArgsConstructor
public class PropertySearchController {

  private final PropertySearchService propertySearchService;

  @GetMapping
  public ResponseEntity<List<PropertyDto>> searchProperties(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "city", required = false) String city,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
      @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
      @RequestParam(name = "startDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
      @RequestParam(name = "endDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
      @RequestParam(name = "propertyType", required = false) PropertyType propertyType) {
    List<Property> properties =
        propertySearchService.searchProperties(keyword, city, status, minPrice, maxPrice, startDate, endDate, propertyType);
    List<PropertyDto> propertyDtoList =
        properties.stream().map(PropertyMapper.INSTANCE::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(propertyDtoList);
  }
}
