package com.conexa.starwars.integration;

import com.conexa.starwars.dto.LoginRequest;
import com.conexa.starwars.dto.RegisterRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLoginBothReturnAUsableJwt() throws Exception {
        RegisterRequest register = new RegisterRequest("luke@rebels.org", "password123", "Luke Skywalker");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("luke@rebels.org"))
                .andExpect(jsonPath("$.role").value("USER"));

        LoginRequest login = new LoginRequest("luke@rebels.org", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void registeringTheSameEmailTwiceIsRejected() throws Exception {
        RegisterRequest register = new RegisterRequest("leia@rebels.org", "password123", "Leia Organa");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        RegisterRequest register = new RegisterRequest("han@rebels.org", "password123", "Han Solo");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest wrongPassword = new LoginRequest("han@rebels.org", "not-the-password");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void loginWithUnknownEmailGivesTheSameMessageAsWrongPassword() throws Exception {
        // the 401 must look the same for wrong password and if account doesn't exist
        LoginRequest neverRegistered = new LoginRequest("nobody-here@rebels.org", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(neverRegistered)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void protectedEndpointRejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/v1/people"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/api/v1/people"));
    }

    @Test
    void healthEndpointIsPublicAndReportsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void registrationRejectsAPasswordThatIsTooShort() throws Exception {
        RegisterRequest tooShortPassword = new RegisterRequest("bad@rebels.org", "123", "Someone");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(tooShortPassword)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationRejectsAPasswordThatIsTooLong() throws Exception {
        RegisterRequest tooLongPassword = new RegisterRequest("bad@rebels.org", "x".repeat(73), "Someone");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(tooLongPassword)))
                .andExpect(status().isBadRequest());
    }
}
