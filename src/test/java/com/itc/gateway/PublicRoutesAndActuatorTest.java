package com.itc.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublicRoutesAndActuatorTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void testPlaceholder() {
        // No-op
    }

}
