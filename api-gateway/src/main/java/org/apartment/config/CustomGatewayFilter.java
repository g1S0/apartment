package org.apartment.config;

import lombok.extern.slf4j.Slf4j;
import org.apartment.client.AuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class CustomGatewayFilter implements GatewayFilter {

  private final AuthClient authClient;

  @Autowired
  @Lazy
  public CustomGatewayFilter(AuthClient authClient) {
    this.authClient = authClient;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader == null) {
      log.warn("Authorization header is missing");
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    if (!authHeader.startsWith("Bearer ")) {
      log.warn("Authorization header does not start with Bearer");
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    log.info("Authorization header found, validating token");

    return Mono.fromCallable(() -> authClient.validateToken(authHeader)).flatMap(userInfo -> {
      log.info("Token validated successfully for user: {}", userInfo);

      ServerHttpRequest mutatedRequest =
          exchange.getRequest().mutate().header("X-User-Id", userInfo).build();

      log.info("Passing mutated request to the next filter");
      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }).onErrorResume(ex -> Mono.defer(() -> {
      log.error("Error occurred during token validation", ex);

      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

      String jsonResponse = "{\"message\": \"Unauthorized\"}";

      return exchange.getResponse().writeWith(
          Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes())));
    }));
  }
}
