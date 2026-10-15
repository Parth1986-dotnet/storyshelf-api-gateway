package com.itc.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtSecurityTest {

    // These tests would normally use TestRestTemplate or WebTestClient with actual server
    // For now, we'll test that the security configuration compiles and basic setup works

    @Test
    void securityConfiguration_compiles() {
        // This test passes if the application context loads without error
        // Actual JWT validation would require integration tests with a real auth server
    }

}
