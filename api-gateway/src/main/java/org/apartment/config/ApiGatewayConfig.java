package org.apartment.config;

import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@AllArgsConstructor
public class ApiGatewayConfig {
  private final GatewayFilter gatewayFilter;

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        // /api/v1/auth
        .route("auth-register", r -> r.path("/api/v1/auth/register").uri("http://localhost:8081"))
        .route("auth-authenticate",
            r -> r.path("/api/v1/auth/authenticate").uri("http://localhost:8081"))
        .route("auth-refresh-token",
            r -> r.path("/api/v1/auth/refresh-token").uri("http://localhost:8081"))

        // /api/v1/users
        .route("users", r -> r.path("/api/v1/users").uri("http://localhost:8081"))

        // POST /api/v1/property
        .route("property-post", r -> r
            .path("/api/v1/property")
            .and().method(HttpMethod.POST)
            .filters(f -> f.filter(gatewayFilter))
            .uri("http://localhost:8082"))

        // GET /api/v1/property
        .route("property-get", r -> r
            .path("/api/v1/property")
            .and().method(HttpMethod.GET)
            .uri("http://localhost:8082"))

        .build();
  }
}
