package org.apartment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apartment.dto.PropertyDto;
import org.apartment.dto.PropertySearchDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
import org.apartment.entity.PropertyType;
import org.apartment.repository.PropertyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PropertySearchControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Container
  public static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test_real_estate_db")
          .withUsername("test_admin").withPassword("test_admin");

  @Container
  public static LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest")).withServices(
          LocalStackContainer.Service.S3);

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
  }

  @BeforeAll
  public static void setUp(@Autowired PropertyRepository propertyRepository) {
    Property property1 = Property.builder().title("Beautiful House in City Center")
        .description("A beautiful house with a garden").type(PropertyType.APARTMENT)
        .price(BigDecimal.valueOf(300000)).city("New York").status(PropertyStatus.AVAILABLE)
        .postedBy(1L).images(new ArrayList<>()).build();

    Property property2 =
        Property.builder().title("Luxury Apartment").description("Modern apartment in downtown")
            .type(PropertyType.CONDO).price(BigDecimal.valueOf(500000)).city("Los Angeles")
            .status(PropertyStatus.SOLD).postedBy(2L).images(new ArrayList<>()).build();

    Property property3 = Property.builder().title("Cozy Studio in Suburbs")
        .description("Affordable studio in the suburbs").type(PropertyType.APARTMENT)
        .price(BigDecimal.valueOf(150000)).city("Chicago").status(PropertyStatus.AVAILABLE)
        .postedBy(3L).images(new ArrayList<>()).build();

    Property property4 =
        Property.builder().title("Beachfront Villa").description("Luxury villa with ocean view")
            .type(PropertyType.APARTMENT).price(BigDecimal.valueOf(1000000)).city("Miami")
            .status(PropertyStatus.AVAILABLE).postedBy(4L).images(new ArrayList<>()).build();

    Property property5 = Property.builder().title("Downtown Office Space")
        .description("Spacious office space in the city center").type(PropertyType.CONDO)
        .price(BigDecimal.valueOf(600000)).city("San Francisco").status(PropertyStatus.SOLD)
        .postedBy(5L).images(new ArrayList<>()).build();

    propertyRepository.saveAll(List.of(property1, property2, property3, property4, property5));
  }

  @Test
  public void testSearchPropertiesByStatusUsingDto() throws Exception {
    PropertySearchDto searchDto = new PropertySearchDto();
    searchDto.setStatus(PropertyStatus.SOLD.name());

    String json = objectMapper.writeValueAsString(searchDto);

    String response = mockMvc.perform(
            get("/api/v1/property/search").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    List<PropertyDto> properties = objectMapper.readValue(response,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyDto.class));

    Assertions.assertEquals(2, properties.size());
    Assertions.assertEquals(PropertyStatus.SOLD, properties.get(0).getStatus());
    Assertions.assertEquals(PropertyStatus.SOLD, properties.get(1).getStatus());
  }

  @Test
  public void testSearchPropertiesByCityUsingDto() throws Exception {
    final String city = "New York";
    PropertySearchDto searchDto = PropertySearchDto.builder().city(city).build();
    String json = objectMapper.writeValueAsString(searchDto);

    String response = mockMvc.perform(
            get("/api/v1/property/search").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    List<PropertyDto> properties = objectMapper.readValue(response,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyDto.class));

    Assertions.assertEquals(1, properties.size());
    Assertions.assertEquals(city, properties.get(0).getCity());
  }

  @Test
  public void testSearchPropertiesByPriceRangeUsingDto() throws Exception {
    PropertySearchDto searchDto = new PropertySearchDto();
    searchDto.setMinPrice(BigDecimal.valueOf(200000));
    searchDto.setMaxPrice(BigDecimal.valueOf(500000));

    String json = objectMapper.writeValueAsString(searchDto);

    String response = mockMvc.perform(
            get("/api/v1/property/search").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    List<PropertyDto> properties = objectMapper.readValue(response,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyDto.class));

    Assertions.assertEquals(2, properties.size());
    Assertions.assertTrue(properties.stream().anyMatch(
        p -> p.getPrice().equals(BigDecimal.valueOf(300000.00).setScale(2, RoundingMode.HALF_UP))));
    Assertions.assertTrue(properties.stream().anyMatch(
        p -> p.getPrice().equals(BigDecimal.valueOf(500000.00).setScale(2, RoundingMode.HALF_UP))));
  }

  @Test
  public void testSearchPropertiesWithNoFilters() throws Exception {
    PropertySearchDto searchDto = new PropertySearchDto();

    String json = objectMapper.writeValueAsString(searchDto);

    String response = mockMvc.perform(
            get("/api/v1/property/search").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    List<PropertyDto> properties = objectMapper.readValue(response,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyDto.class));

    Assertions.assertEquals(5, properties.size());
  }

  @Test
  public void testSearchPropertiesByDescriptionUsingDto() throws Exception {
    final String descriptionKeyword = "modern";
    PropertySearchDto searchDto = PropertySearchDto.builder().keyword(descriptionKeyword).build();

    String json = objectMapper.writeValueAsString(searchDto);

    String response = mockMvc.perform(
            get("/api/v1/property/search").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    List<PropertyDto> properties = objectMapper.readValue(response,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyDto.class));

    Assertions.assertEquals(1, properties.size());
    Assertions.assertTrue(properties.stream().anyMatch(
        p -> p.getDescription().toLowerCase().contains(descriptionKeyword.toLowerCase())));
  }
}