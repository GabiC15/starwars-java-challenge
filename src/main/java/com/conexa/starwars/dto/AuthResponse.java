package com.conexa.starwars.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String email,
        String role
) {
    public static AuthResponse bearer(String token, long expiresInSeconds, String email, String role) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, email, role);
    }
}
