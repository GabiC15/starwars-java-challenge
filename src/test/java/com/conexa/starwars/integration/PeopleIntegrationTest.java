package com.conexa.starwars.integration;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.RegisterRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PeopleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeEach
    void issueToken() throws Exception {
        RegisterRequest register = new RegisterRequest("rey@rebels.org", "password123", "Rey Skywalker");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andReturn();
        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        bearerToken = "Bearer " + auth.token();
    }

    @Test
    void listsPeopleUsingSwapiNativePagination() throws Exception {
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "ok",
                                  "total_records": 2,
                                  "total_pages": 1,
                                  "previous": null,
                                  "next": null,
                                  "results": [
                                    {"uid": "1", "description": "person", "properties": {"name": "Luke Skywalker", "height": "172"}},
                                    {"uid": "2", "description": "person", "properties": {"name": "C-3PO", "height": "167"}}
                                  ]
                                }
                                """)));

        mockMvc.perform(get("/api/v1/people").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.content[1].name").value("C-3PO"));
    }

    @Test
    void filtersByIdAgainstTheSwapiDetailEndpoint() throws Exception {
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/1/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "ok",
                                  "result": {"uid": "1", "description": "person", "properties": {"name": "Luke Skywalker", "height": "172"}}
                                }
                                """)));

        mockMvc.perform(get("/api/v1/people?id=1").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"));
    }

    @Test
    void filtersByAnUnknownIdReturns404() throws Exception {
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/999/"))
                .willReturn(aResponse().withStatus(404)));

        mockMvc.perform(get("/api/v1/people?id=999").header("Authorization", bearerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersByNameUsingSwapisSearch() throws Exception {
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "ok",
                                  "result": [
                                    {"uid": "1", "description": "person", "properties": {"name": "Luke Skywalker", "height": "172"}}
                                  ]
                                }
                                """)));

        mockMvc.perform(get("/api/v1/people?name=luke").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"));
    }

    @Test
    void pageBeyondTheLastOneIsEmpty() throws Exception {
        // Swapi itself re-serves the last page instead of an empty one
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "ok",
                                  "total_records": 12,
                                  "total_pages": 3,
                                  "results": [
                                    {"uid": "1", "description": "person", "properties": {"name": "Luke Skywalker", "height": "172"}}
                                  ]
                                }
                                """)));

        mockMvc.perform(get("/api/v1/people?page=6&size=5").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void secondLookupByIdUsesTheCache() throws Exception {
        // id=42 isn't used elsewhere in this class, so the cache is guaranteed cold here
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/42/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "ok",
                                  "result": {"uid": "42", "description": "person", "properties": {"name": "Wedge Antilles", "height": "170"}}
                                }
                                """)));

        mockMvc.perform(get("/api/v1/people?id=42").header("Authorization", bearerToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/people?id=42").header("Authorization", bearerToken))
                .andExpect(status().isOk());

        SWAPI.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/api/people/42/")));
    }

    @Test
    void swapiReturningAServerErrorBecomesA502() throws Exception {
        SWAPI.stubFor(WireMock.get(urlPathEqualTo("/api/people/"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(get("/api/v1/people?page=7&size=3").header("Authorization", bearerToken))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(502));
    }
}
