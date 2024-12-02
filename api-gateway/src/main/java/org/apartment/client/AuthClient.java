package org.apartment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth")
public interface AuthClient {

  @PostMapping("/api/v1/auth/validate-token")
  Integer validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader);
}
