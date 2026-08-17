package com.conexa.starwars.integration;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.RegisterRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// lower thresholds just for this test class
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.swapi.sliding-window-size=" + CircuitBreakerIntegrationTest.FORCED_FAILURES,
        "resilience4j.circuitbreaker.instances.swapi.minimum-number-of-calls=" + CircuitBreakerIntegrationTest.FORCED_FAILURES,
        "resilience4j.circuitbreaker.instances.swapi.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.swapi.wait-duration-in-open-state=10s"
})
class CircuitBreakerIntegrationTest extends AbstractIntegrationTest {

    static final int FORCED_FAILURES = 4;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeEach
    void issueToken() throws Exception {
        RegisterRequest register = new RegisterRequest("han@rebels.org", "password123", "Han Solo");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andReturn();
        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        bearerToken = "Bearer " + auth.token();
    }

    @Test
    void openCircuitReturns503WithoutCallingSwapiAgain() throws Exception {
        SWAPI.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/people/"))
                .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < FORCED_FAILURES; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/people?page=1&size=1")
                            .header("Authorization", bearerToken))
                    .andExpect(status().isBadGateway());
        }
        // breaker is open now and this call must short circuit
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/people?page=1&size=1")
                        .header("Authorization", bearerToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(503));

        SWAPI.verify(FORCED_FAILURES, getRequestedFor(urlPathEqualTo("/api/people/")));
    }
}
