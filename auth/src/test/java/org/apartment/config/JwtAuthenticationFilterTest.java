package org.apartment.config;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apartment.dto.ResponseDto;
import org.junit.jupiter.api.Test;

class JwtAuthenticationFilterTest {

  private final JwtAuthenticationFilter jwtAuthenticationFilter =
      new JwtAuthenticationFilter(null, null, null);

  @Test
  void convertObjectToJson_ShouldConvertObjectToJson() throws JsonProcessingException {
    ResponseDto<String> responseDto = new ResponseDto<>("Some data");

    String json = jwtAuthenticationFilter.convertObjectToJson(responseDto);
    assertNotNull(json);

    assertTrue(json.contains("\"message\":\"Some data\""));
  }

  @Test
  void convertObjectToJson_ShouldReturnNull_WhenObjectIsNull() throws JsonProcessingException {
    String json = jwtAuthenticationFilter.convertObjectToJson(null);

    assertNull(json, null);
  }
}