package org.apartment.config;

import org.apartment.client.AuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
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
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    return Mono.fromCallable(() -> authClient.validateToken(authHeader)).flatMap(userInfo -> {
      ServerHttpRequest mutatedRequest =
          exchange.getRequest().mutate().header("X-User-Id", String.valueOf(userInfo)).build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    });
  }
}
