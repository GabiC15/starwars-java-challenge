package com.conexa.starwars.service;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.LoginRequest;
import com.conexa.starwars.dto.RegisterRequest;
import com.conexa.starwars.common.exception.DuplicateUserException;
import com.conexa.starwars.security.JwtService;
import com.conexa.starwars.model.Role;
import com.conexa.starwars.model.User;
import com.conexa.starwars.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** Creates the user with an encoded password and hands back a token. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("A user with email " + email + " already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.USER)
                .build();
        try {
            // flush now so a concurrent duplicate hits the unique constraint
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException("A user with email " + email + " already exists");
        }

        return buildAuthResponse(email, Role.USER.name());
    }

    /** Delegates to AuthenticationManager, then issues a token. */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        return buildAuthResponse(authentication.getName(), role);
    }

    private AuthResponse buildAuthResponse(String email, String role) {
        String token = jwtService.generateToken(email);
        return AuthResponse.bearer(token, jwtService.expirationSeconds(), email, role);
    }
}
