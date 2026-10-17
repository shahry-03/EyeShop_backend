package com.eyeshop.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Externalized CORS configuration.
 * Override per environment via application-{profile}.yaml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Allowed origin patterns. Use patterns for wildcard subdomains.
     * Examples: "http://localhost:3000", "https://*.eyeshop.com"
     */
    private List<String> allowedOriginPatterns = List.of(
        "http://localhost:3000",
        "http://localhost:5173"     // Vite default
    );

    /** HTTP methods allowed from browser. */
    private List<String> allowedMethods = List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    );

    /** Headers client can send. */
    private List<String> allowedHeaders = List.of("*");

    /** Headers client can read from response. */
    private List<String> exposedHeaders = List.of(
        "Set-Cookie",
        "Authorization",
        "X-Account-Locked-Until",
        "Retry-After"
    );

    /**
     * Allow cookies (refresh token). Must be true if using HTTP-only cookies.
     * When true, allowedOriginPatterns cannot be "*" — must be explicit.
     */
    private boolean allowCredentials = true;

    /** Preflight cache in seconds. Reduces OPTIONS requests. */
    private long maxAge = 3600;
}