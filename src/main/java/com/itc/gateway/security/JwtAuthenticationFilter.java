package com.itc.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Propagates validated JWT claims as request headers to downstream services.
 * Runs after Spring Security has already validated the token.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        var mutated = exchange.getRequest().mutate()
                                .header("X-User-Id", getClaimOrEmpty(jwt, "sub"))
                                .header("X-User-Email", getClaimOrEmpty(jwt, "email"))
                                .header("X-User-Roles", String.join(",", jwtAuth.getAuthorities()
                                        .stream().map(Object::toString).toList()))
                                .build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Run after Spring Security (-100) but before routing (Integer.MIN_VALUE + 50)
        return -90;
    }

    private String getClaimOrEmpty(Jwt jwt, String claim) {
        Object val = jwt.getClaim(claim);
        return val != null ? val.toString() : "";
    }
}
