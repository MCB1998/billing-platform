package com.mcb.billing.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security rules for the gateway, which acts as an OAuth2 <em>resource server</em>: it
 * only validates the JWTs that Keycloak issues. It never authenticates users itself -
 * there is no login page and no redirect here; callers arrive with a token already.
 *
 * <p>Because the gateway runs on WebFlux, this is the reactive Spring Security API
 * ({@link SecurityWebFilterChain} / {@link ServerHttpSecurity}) rather than the servlet
 * {@code HttpSecurity} seen in most tutorials.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // No CSRF protection needed: this is a stateless API authenticated by a
                // Bearer token, not by a session cookie a browser would send implicitly.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Liveness/readiness must stay reachable without a token.
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Everything else - including the proxied service routes and the
                        // route table, which would reveal the internal topology.
                        .anyExchange().authenticated())
                // Validate incoming "Authorization: Bearer <jwt>" headers. The decoder is
                // built from the configured JWK Set URI (Keycloak's public keys).
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }
}
