package com.itc.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Stateless — no session
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // Validate JWT on every protected route
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}) // issuer-uri configured in application.yaml
                )

                .authorizeExchange(exchanges -> exchanges
                        // Auth service is public
                        .pathMatchers("/api/auth/**").permitAll()
                        // Public book browsing (GET only)
                        .pathMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        // Actuator — restricted separately via management port / IP in yaml
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/actuator/**").authenticated()
                        // Protected routes require a valid JWT
                        .pathMatchers("/api/orders/**").authenticated()
                        .pathMatchers("/api/payments/**").authenticated()
                        .pathMatchers("/api/**").authenticated()
                        // Deny everything else
                        .anyExchange().denyAll()
                )
                .build();
    }

    /**
     * WebFilter to add security headers to all responses
     */
    @Bean
    public WebFilter securityHeadersWebFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
            exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
            exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
            exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
            exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            return chain.filter(exchange);
        };
    }
}
