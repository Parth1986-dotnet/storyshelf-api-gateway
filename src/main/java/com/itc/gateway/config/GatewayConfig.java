package com.itc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;

@Configuration
public class GatewayConfig {

    @Value("${services.auth-url:http://localhost:8089}")
    private String authServiceUrl;

    @Value("${services.book-url:http://localhost:8087}")
    private String bookServiceUrl;

    @Value("${services.order-url:http://localhost:8090}")
    private String orderServiceUrl;

    @Value("${services.payment-url:http://localhost:8091}")
    private String paymentServiceUrl;

    @Value("${services.monolith-url:http://localhost:8081}")
    private String monolithServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("auth-service", route -> route
                        .path("/api/auth/**")
                        .uri(authServiceUrl)
                )

                .route("book-service", route -> route
                        .path("/api/books/**")
                        .uri(bookServiceUrl)
                )

                .route("order-service", route -> route
                        .path("/api/orders/**")
                        .uri(orderServiceUrl)
                )

                .route("payment-service", route -> route
                        .path("/api/payments/**")
                        .filters(filters -> filters.filter((exchange, chain) -> {

                            HttpCookie cookie = exchange.getRequest()
                                    .getCookies()
                                    .getFirst("accessToken");

                            if (cookie == null) {
                                cookie = exchange.getRequest()
                                        .getCookies()
                                        .getFirst("access_token");
                            }

                            if (cookie != null) {

                                String token = cookie.getValue();

                                return chain.filter(
                                        exchange.mutate()
                                                .request(request ->
                                                        request.headers(headers ->
                                                                headers.setBearerAuth(token)
                                                        )
                                                )
                                                .build()
                                );
                            }

                            return chain.filter(exchange);
                        }))
                        .uri(paymentServiceUrl)
                )

                // This fallback route must remain last.
                .route("monolith-service", route -> route
                        .path("/api/**")
                        .uri(monolithServiceUrl)
                )

                .build();
    }
}