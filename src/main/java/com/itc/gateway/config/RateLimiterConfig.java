package com.itc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit per authenticated user (JWT sub claim).
     * Falls back to IP address for unauthenticated requests.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> "user:" + principal.getName())
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    var addr = exchange.getRequest().getRemoteAddress();
                    return "ip:" + (addr != null ? addr.getAddress().getHostAddress() : "unknown");
                }));
    }

    /** Authenticated routes: configurable req/s, burst capacity */
    @Bean
    public RedisRateLimiter authRoutesRateLimiter(
            @Value("${rate-limiting.auth.requests-per-second:20}") int requestsPerSecond,
            @Value("${rate-limiting.auth.burst-capacity:40}") int burstCapacity) {
        return new RedisRateLimiter(requestsPerSecond, burstCapacity, 1);
    }

    /** Public routes: configurable req/s, burst capacity */
    @Bean
    public RedisRateLimiter publicRoutesRateLimiter(
            @Value("${rate-limiting.public.requests-per-second:10}") int requestsPerSecond,
            @Value("${rate-limiting.public.burst-capacity:20}") int burstCapacity) {
        return new RedisRateLimiter(requestsPerSecond, burstCapacity, 1);
    }

    /**
     * Creates a timer metric for monitoring rate limiter rejections
     */
    @Bean
    public Timer rateLimiterRejectionTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.ratelimiter.rejections")
                .description("Time spent handling rate limiter rejections")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }
}
