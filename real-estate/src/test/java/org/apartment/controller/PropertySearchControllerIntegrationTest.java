package org.apartment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apartment.dto.PropertyDto;
import org.apartment.dto.PropertySearchDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PropertySearchControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private EntityManager entityManager;

  @Container
  public static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test_real_estate_db")
          .withUsername("test_admin").withPassword("test_admin");

  static {
    postgresContainer.start();
  }

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
  public void setUp() throws InterruptedException {
    entityManager = entityManager.getEntityManagerFactory().createEntityManager();
    SearchSession searchSession = Search.session(entityManager);

    searchSession.massIndexer(Property.class).threadsToLoadObjects(5).startAndWait();
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