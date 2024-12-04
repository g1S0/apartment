package org.apartment.auth.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apartment.auth.dto.AuthenticationRequestDto;
import org.apartment.auth.dto.AuthenticationResponseDto;
import org.apartment.auth.dto.ChangePasswordRequestDto;
import org.apartment.auth.dto.RegisterRequestDto;
import org.apartment.auth.entity.User;
import org.apartment.auth.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SecurityIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @MockBean
  private KafkaTemplate<String, String> kafkaTemplate;

  @Container
  public static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:latest").withDatabaseName("test_user_auth_db")
          .withUsername("test_admin").withPassword("test_admin");

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  private final String password = "Password@123)";

  private static final String REGISTER_URL = "/api/v1/auth/register";
  private static final String AUTHENTICATE_URL = "/api/v1/auth/authenticate";
  private static final String CHANGE_PASSWORD_URL = "/api/v1/users";

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
  }

  public String getUniqueEmail() {
    return UUID.randomUUID().toString().substring(0, 10) + "@example.com";
  }

  private AuthenticationResponseDto registerUser(String email) throws Exception {
    RegisterRequestDto registerRequest =
        RegisterRequestDto.builder().firstName("John").secondName("Doe").email(email)
            .password(password).build();

    KafkaOperations template = mock(KafkaOperations.class);
    CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
    given(template.send(any(Message.class))).willReturn(future);

    String registerRequestJson = new ObjectMapper().writeValueAsString(registerRequest);
    MvcResult mvcResult = mockMvc.perform(
            post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(registerRequestJson))
        .andExpect(status().isOk()).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    return objectMapper.readValue(responseJson, AuthenticationResponseDto.class);
  }

  private AuthenticationResponseDto authenticateUser(String email) throws Exception {
    AuthenticationRequestDto authRequest = new AuthenticationRequestDto();
    authRequest.setEmail(email);
    authRequest.setPassword(password);

    String authRequestJson = objectMapper.writeValueAsString(authRequest);
    MvcResult mvcResult = mockMvc.perform(
            post(AUTHENTICATE_URL).contentType(MediaType.APPLICATION_JSON).content(authRequestJson))
        .andExpect(status().isOk()).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    return objectMapper.readValue(responseJson, AuthenticationResponseDto.class);
  }

  @Test
  void testRegister() throws Exception {
    String email = getUniqueEmail();
    AuthenticationResponseDto token = registerUser(email);
    assertNotNull(token.getAccessToken());
    assertNotNull(token.getRefreshToken());
    Optional<User> user = userRepository.findByEmail(email);
    Assertions.assertTrue(user.isPresent());
  }

  @Test
  void testAuthenticateAndRefreshToken() throws Exception {
    String email = getUniqueEmail();
    AuthenticationResponseDto registrationResponse = registerUser(email);
    assertNotNull(registrationResponse);
    assertNotNull(registrationResponse.getAccessToken());
    assertNotNull(registrationResponse.getRefreshToken());

    AuthenticationResponseDto authResponse = authenticateUser(email);
    assertNotNull(authResponse);
    assertNotNull(authResponse.getAccessToken());
    assertNotNull(authResponse.getRefreshToken());
  }

  @Test
  public void testChangePasswordFlow() throws Exception {
    String email = getUniqueEmail();
    AuthenticationResponseDto registrationResponse = registerUser(email);
    assertNotNull(registrationResponse);

    String accessToken = registrationResponse.getAccessToken();

    final String newPassword = "NewPassword@456)";
    ChangePasswordRequestDto changePasswordRequest =
        new ChangePasswordRequestDto(password, newPassword, newPassword);

    String changePasswordJson = objectMapper.writeValueAsString(changePasswordRequest);

    mockMvc.perform(
            put(CHANGE_PASSWORD_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON).content(changePasswordJson))
        .andExpect(status().isOk());

    AuthenticationRequestDto newAuthRequest = new AuthenticationRequestDto();
    newAuthRequest.setEmail(email);
    newAuthRequest.setPassword(newPassword);

    String newAuthRequestJson = objectMapper.writeValueAsString(newAuthRequest);

    mockMvc.perform(
            post(AUTHENTICATE_URL).contentType(MediaType.APPLICATION_JSON).content(newAuthRequestJson))
        .andExpect(status().isOk());

    AuthenticationRequestDto oldAuthRequest = new AuthenticationRequestDto();
    oldAuthRequest.setEmail(email);
    oldAuthRequest.setPassword(password);

    String oldAuthRequestJson = objectMapper.writeValueAsString(oldAuthRequest);

    mockMvc.perform(
            post(AUTHENTICATE_URL).contentType(MediaType.APPLICATION_JSON).content(oldAuthRequestJson))
        .andExpect(status().isUnauthorized());
  }
}