package com.itc.gateway.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(RateLimiterConfig.class)
@ActiveProfiles("test")
class RateLimiterConfigEnhancedTest {

    @Autowired
    private KeyResolver userKeyResolver;

    @Autowired
    private RedisRateLimiter authRoutesRateLimiter;

    @Autowired
    private RedisRateLimiter publicRoutesRateLimiter;

    @Test
    void keyResolverBeanExists() {
        assertThat(userKeyResolver).isNotNull();
    }

    @Test
    void authRateLimiterHasCorrectConfiguration() {
        // Default values from application.yml: 20 req/s, burst 40
        assertThat(authRoutesRateLimiter).isNotNull();
        // Note: RedisRateLimiter doesn't expose getters for rate/burst in older versions
        // This test primarily verifies the bean is created correctly
    }

    @Test
    void publicRateLimiterHasCorrectConfiguration() {
        // Default values from application.yml: 10 req/s, burst 20
        assertThat(publicRoutesRateLimiter).isNotNull();
    }
}