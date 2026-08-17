package com.conexa.starwars.service;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.LoginRequest;
import com.conexa.starwars.dto.RegisterRequest;
import com.conexa.starwars.common.exception.DuplicateUserException;
import com.conexa.starwars.security.JwtService;
import com.conexa.starwars.model.Role;
import com.conexa.starwars.model.User;
import com.conexa.starwars.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    private static final String SECRET = "s0HWJWdaxcodruBfjspwhhcuh7wJaB6UVssVMF+cor0=";

    @BeforeEach
    void setUp() {
        var passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        var jwtService = new JwtService(new com.conexa.starwars.security.JwtProperties(
                SECRET,
                java.time.Duration.ofMinutes(5)));
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerRejectsAnEmailThatAlreadyExists() {
        when(userRepository.existsByEmail("leia@rebels.org")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("leia@rebels.org", "password123", "Leia Organa");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void registerSavesAnEncodedPasswordAndReturnsAToken() {
        when(userRepository.existsByEmail("leia@rebels.org")).thenReturn(false);
        RegisterRequest request = new RegisterRequest("Leia@Rebels.org", "password123", "Leia Organa");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("leia@rebels.org");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(response.email()).isEqualTo("leia@rebels.org");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginDelegatesToTheAuthenticationManagerAndReturnsAToken() {
        var principal = org.springframework.security.core.userdetails.User.builder()
                .username("han@rebels.org").password("irrelevant").authorities("ROLE_USER").build();
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(successfulAuth);

        AuthResponse response = authService.login(new LoginRequest("han@rebels.org", "password123"));

        assertThat(response.email()).isEqualTo("han@rebels.org");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginRethrowsBadCredentialsException() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));
        LoginRequest wrongPassword = new LoginRequest("han@rebels.org", "wrong");

        assertThatThrownBy(() -> authService.login(wrongPassword))
                .isInstanceOf(BadCredentialsException.class);
    }
}
