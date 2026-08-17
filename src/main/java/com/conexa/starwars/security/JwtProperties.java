package com.conexa.starwars.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be configured");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(1);
        }
    }
}
