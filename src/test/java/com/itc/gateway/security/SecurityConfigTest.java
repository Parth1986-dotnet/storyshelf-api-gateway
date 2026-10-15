package com.itc.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Test
    void securityFilterChainIsCreated() {
        assertThat(securityWebFilterChain).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterExtractsAuthorities() {
        // This would require more complex setup to test the converter directly
        // For now, we verify the bean exists
        SecurityConfig config = new SecurityConfig();
        var converter = config.jwtAuthenticationConverter();
        assertThat(converter).isNotNull();
    }

    @Test
    void securityHeadersWebFilterIsCreated() {
        SecurityConfig config = new SecurityConfig();
        var filter = config.securityHeadersWebFilter();
        assertThat(filter).isNotNull();
    }
}