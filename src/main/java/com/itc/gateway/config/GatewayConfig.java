package com.itc.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // 🔐 Auth Service
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8089")
                )

                // 📚 Book Service
                .route("book-service", r -> r
                        .path("/api/books/**")
                        .uri("http://localhost:8087")
                )

                // 🛒 Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://localhost:8090")
                )

                // 💳 Payment Service (COOKIE → AUTH HEADER)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter((exchange, chain) -> {
                            HttpCookie cookie = exchange.getRequest()
                                    .getCookies()
                                    .getFirst("accessToken");

                            if (cookie != null) {
                                return chain.filter(
                                        exchange.mutate()
                                                .request(req -> req.headers(h ->
                                                        h.setBearerAuth(cookie.getValue())
                                                ))
                                                .build()
                                );
                            }
                            return chain.filter(exchange);
                        }))
                        .uri("http://localhost:8091")
                )

                // 🧱 FALLBACK — MUST BE LAST
                .route("monolith-service", r -> r
                        .path("/api/**")
                        .uri("http://localhost:8081")
                )

                .build();
    }
}

