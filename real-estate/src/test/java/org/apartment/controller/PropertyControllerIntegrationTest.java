package org.apartment.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apartment.dto.PropertyDto;
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

  @Test
  public void testGetPropertiesPage1() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/v1/property?page=0&size=2")).andExpect(status().isOk())
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
        mockMvc.perform(get("/api/v1/property?page=1&size=2")).andExpect(status().isOk())
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    List<PropertyDto> properties = getResponsePageDTO(response);

    assertThat(properties, hasSize(2));
    assertThat(properties.get(0).getTitle(), is("Cozy Studio in Suburbs"));
    assertThat(properties.get(1).getTitle(), is("Beachfront Villa"));
  }

  @Test
  public void testGetNonExistentPage() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/property?page=10&size=2"))
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