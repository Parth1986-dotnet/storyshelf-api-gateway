package com.itc.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void publicRoutesAreAccessibleWithoutAuthentication() {
        // Test that public routes (like /api/books/** with GET) are accessible
        webTestClient.get()
                .uri("/api/books/")
                .exchange()
                // Expect either 200 OK (if service is available) or 503/504 (if downstream service unavailable)
                // but NOT 401 Unauthorized or 403 Forbidden
                .expectStatus()
                .isNotEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .isNotEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void actuatorEndpointsAreProtected() {
        // Test that sensitive actuator endpoints require authentication
        webTestClient.get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus()
                .isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @Test
    void actuatorHealthAndInfoArePublic() {
        // Test that basic actuator endpoints are public
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient.get()
                .uri("/actuator/info")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void securityHeadersArePresent() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectHeader().exists("X-Content-Type-Options")
                .expectHeader().exists("X-Frame-Options")
                .expectHeader().exists("X-XSS-Protection")
                .expectHeader().exists("Referrer-Policy")
                .expectHeader().exists("Strict-Transport-Security");
    }

    @Test
    void corsHeadersArePresent() {
        webTestClient.options()
                .uri("/api/books/")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
    }
}