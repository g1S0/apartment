package org.apartment.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apartment.dto.ResponseDto;
import org.apartment.repository.TokenRepository;
import org.apartment.service.JwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;

  private final String bearerPrefix = "Bearer ";
  private final int bearerPrefixLength = bearerPrefix.length();

  public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService,
                                 TokenRepository tokenRepository) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.tokenRepository = tokenRepository;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws IOException {
    try {
      if (request.getServletPath().contains("/api/v1/auth")
          && !request.getServletPath().contains("/validate-token")) {
        filterChain.doFilter(request, response);
        return;
      }
      final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (authHeader == null || !authHeader.startsWith(bearerPrefix)) {
        filterChain.doFilter(request, response);
        return;
      }
      final String jwt = authHeader.substring(bearerPrefixLength);
      final String userEmail = jwtService.extractUsername(jwt);
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        if (jwtService.isTokenValid(jwt, userDetails)) {
          var isTokenValid =
              tokenRepository.findTokenByValue(jwt).map(t -> !t.isRevoked()).orElse(false);
          if (isTokenValid) {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        }
      }
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException |
             AuthenticationException e) {
      ResponseDto<String> errorResponse = new ResponseDto<>(e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(convertObjectToJson(errorResponse));
    } catch (Exception e) {
      ResponseDto<String> errorResponse = new ResponseDto<>(e.getMessage());
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      response.getWriter().write(convertObjectToJson(errorResponse));
    }
  }

  public String convertObjectToJson(Object object) throws JsonProcessingException {
    if (object == null) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }
}
