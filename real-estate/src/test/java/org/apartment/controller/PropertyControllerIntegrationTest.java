package org.apartment.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apartment.dto.PropertyDto;
import org.apartment.entity.Property;
import org.apartment.entity.PropertyStatus;
import org.apartment.entity.PropertyType;
import org.apartment.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PropertyControllerIntegrationTest {

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
        .postedBy(123L).images(new ArrayList<>()).build();

    Property property2 =
        Property.builder().title("Luxury Apartment").description("Modern apartment in downtown")
            .type(PropertyType.CONDO).price(BigDecimal.valueOf(500000)).city("Los Angeles")
            .status(PropertyStatus.SOLD).postedBy(124L).images(new ArrayList<>()).build();

    Property property3 = Property.builder().title("Cozy Studio in Suburbs")
        .description("Affordable studio in the suburbs").type(PropertyType.APARTMENT)
        .price(BigDecimal.valueOf(150000)).city("Chicago").status(PropertyStatus.AVAILABLE)
        .postedBy(125L).images(new ArrayList<>()).build();

    Property property4 =
        Property.builder().title("Beachfront Villa").description("Luxury villa with ocean view")
            .type(PropertyType.APARTMENT).price(BigDecimal.valueOf(1000000)).city("Miami")
            .status(PropertyStatus.AVAILABLE).postedBy(126L).images(new ArrayList<>()).build();

    Property property5 = Property.builder().title("Downtown Office Space")
        .description("Spacious office space in the city center").type(PropertyType.CONDO)
        .price(BigDecimal.valueOf(600000)).city("San Francisco").status(PropertyStatus.SOLD)
        .postedBy(127L).images(new ArrayList<>()).build();

    propertyRepository.saveAll(List.of(property1, property2, property3, property4, property5));
  }

  @Test
  public void testGetPropertiesPage1() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/v1/property/properties?page=0&size=2")).andExpect(status().isOk())
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    List<PropertyDto> properties = getResponsePageDTO(response);

    assertThat(properties, hasSize(2));

    assertThat(properties.get(0).getTitle(), is("Beautiful House in City Center"));
    assertThat(properties.get(1).getTitle(), is("Luxury Apartment"));
  }

  @Test
  public void testGetPropertiesPage2() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/v1/property/properties?page=1&size=2")).andExpect(status().isOk())
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    List<PropertyDto> properties = getResponsePageDTO(response);

    assertThat(properties, hasSize(2));
    assertThat(properties.get(0).getTitle(), is("Cozy Studio in Suburbs"));
    assertThat(properties.get(1).getTitle(), is("Beachfront Villa"));
  }

  @Test
  public void testGetNonExistentPage() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/property/properties?page=10&size=2"))
        .andExpect(status().isOk()).andReturn();

    MockHttpServletResponse response = result.getResponse();
    List<PropertyDto> properties = getResponsePageDTO(response);

    assertThat(properties, hasSize(0));
  }

  private List<PropertyDto> getResponsePageDTO(MockHttpServletResponse response)
      throws IOException {
    String responseBody = response.getContentAsString();
    Map<String, Object> objectMap = objectMapper.readValue(responseBody, Map.class);

    List<Object> content = (List<Object>) objectMap.get("content");

    List<PropertyDto> result = new ArrayList<>();
    for (Object o : content) {
      PropertyDto propertyDto = objectMapper.convertValue(o, PropertyDto.class);
      result.add(propertyDto);
    }
    return result;
  }
}