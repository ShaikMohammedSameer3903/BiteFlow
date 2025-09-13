package com.fooddelivery.apigateway.filter;

import com.fooddelivery.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip authentication for public endpoints
            String path = request.getURI().getPath();
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            try {
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information and add to headers for downstream services
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                // Add user information to request headers
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Email", username)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "JWT token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.equals("/api/users/register") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/api/restaurants") && !path.contains("/my-restaurants") ||
               path.startsWith("/api/menu/restaurant/") ||
               path.startsWith("/api/deliveries/track/");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"" + err + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
