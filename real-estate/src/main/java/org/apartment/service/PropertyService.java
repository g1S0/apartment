package org.apartment.service;

import lombok.AllArgsConstructor;
import org.apartment.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.apartment.entity.Property;

import java.util.List;

@Service
@AllArgsConstructor
public class PropertyService {

    private PropertyRepository propertyRepository;

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }
}
