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
        .route("auth-register", r -> r.path("/api/v1/auth/register").uri("lb://auth"))
        .route("auth-authenticate", r -> r.path("/api/v1/auth/authenticate").uri("lb://auth"))
        .route("auth-refresh-token", r -> r.path("/api/v1/auth/refresh-token").uri("lb://auth"))

        // /api/v1/users
        .route("users", r -> r.path("/api/v1/users").and().method(HttpMethod.PUT).uri("lb://auth"))
        .route("auth-delete-user",
            r -> r.path("/api/v1/users").and().method(HttpMethod.DELETE).uri("lb://auth"))

        // POST /api/v1/property
        .route("property-post", r -> r.path("/api/v1/property").and().method(HttpMethod.POST)
            .filters(f -> f.filter(gatewayFilter)).uri("lb://real-estate"))

        // GET /api/v1/property
        .route("property-get",
            r -> r.path("/api/v1/property").and().method(HttpMethod.GET).uri("lb://real-estate"))

        // GET /api/v1/property/search
        .route("property-get", r -> r.path("/api/v1/property/search").and().method(HttpMethod.GET)
            .uri("lb://real-estate"))

        .build();
  }
}