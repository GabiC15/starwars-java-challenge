package com.conexa.starwars.controller;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.LoginRequest;
import com.conexa.starwars.dto.RegisterRequest;
import com.conexa.starwars.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Register and login. These two are public, everything else in the API needs
 * the token they give you back.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Registration and login. Both endpoints are public and return a JWT to use as a Bearer token on every other endpoint.")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Create a new user account and return a JWT for it")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate an existing user and return a JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
