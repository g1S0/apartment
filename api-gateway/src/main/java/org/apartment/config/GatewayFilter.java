package org.apartment.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayFilter implements org.springframework.cloud.gateway.filter.GatewayFilter {

  private final WebClient.Builder webClientBuilder;

  public GatewayFilter(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    return webClientBuilder.build().post().uri("http://localhost:8081/api/v1/auth/validate-token")
        .header(HttpHeaders.AUTHORIZATION, authHeader).retrieve()
        .onStatus(HttpStatusCode::is4xxClientError,
            clientResponse -> Mono.error(new RuntimeException("Invalid token")))
        .bodyToMono(Integer.class).flatMap(userInfo -> {
          ServerHttpRequest mutatedRequest =
              exchange.getRequest().mutate().header("X-User-Id", String.valueOf(userInfo)).build();

          return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
  }
}
