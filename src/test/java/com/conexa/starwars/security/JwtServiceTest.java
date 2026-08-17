package com.conexa.starwars.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "sc+R8pp5jFHIZk9MSbV5peR6/DBZLWVkDyj8FzzStLM=";
    private static final String OTHER_SECRET = "oBqDWnlFd+IFG6GEHZM3Rnt/vT31MmwCO8CDteOmms8=";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5)));

    private UserDetails user(String email) {
        return User.builder().username(email).password("irrelevant").authorities("ROLE_USER").build();
    }

    @Test
    void generatesATokenThatIsValidForTheSameUser() {
        UserDetails luke = user("luke@rebels.org");

        String token = jwtService.generateToken(luke.getUsername());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("luke@rebels.org");
        assertThat(jwtService.isValid(token, luke)).isTrue();
    }

    @Test
    void tokenIsNotValidForADifferentUser() {
        String token = jwtService.generateToken("luke@rebels.org");

        assertThat(jwtService.isValid(token, user("vader@empire.gov"))).isFalse();
    }

    @Test
    void anExpiredTokenIsNotValid() {
        // token is issued 2 hours ago with 1 minute of validity and it's already expired
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        JwtService pastJwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(1)), twoHoursAgo);
        UserDetails luke = user("luke@rebels.org");

        String token = pastJwtService.generateToken(luke.getUsername());

        assertThat(jwtService.isValid(token, luke)).isFalse();
    }

    @Test
    void garbageTokenIsNotValid() {
        assertThat(jwtService.isValid("not-real-token", user("luke@rebels.org"))).isFalse();
    }

    @Test
    void aWellFormedTokenSignedWithADifferentSecretIsNotValid() {
        // a well-formed token signed with the wrong key
        JwtService otherJwtService = new JwtService(new JwtProperties(OTHER_SECRET, Duration.ofMinutes(5)));
        UserDetails luke = user("luke@rebels.org");

        String forgedToken = otherJwtService.generateToken(luke.getUsername());

        assertThat(jwtService.isValid(forgedToken, luke)).isFalse();
    }
}
