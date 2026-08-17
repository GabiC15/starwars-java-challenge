package com.conexa.starwars.integration;

import com.conexa.starwars.dto.AuthResponse;
import com.conexa.starwars.dto.RegisterRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// films are paginated in memory, unlike people, so this covers that separate code path
class FilmIntegrationTest extends AbstractIntegrationTest {

    private static final String FOUR_FILMS_UNPAGINATED = """
            {
              "message": "ok",
              "result": [
                {"uid": "1", "description": "film", "properties": {"title": "A New Hope", "episode_id": 4}},
                {"uid": "2", "description": "film", "properties": {"title": "The Empire Strikes Back", "episode_id": 5}},
                {"uid": "3", "description": "film", "properties": {"title": "Return of the Jedi", "episode_id": 6}},
                {"uid": "4", "description": "film", "properties": {"title": "The Phantom Menace", "episode_id": 1}}
              ]
            }
            """;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeEach
    void issueToken() throws Exception {
        RegisterRequest register = new RegisterRequest("obiwan@rebels.org", "password123", "Obi-Wan Kenobi");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andReturn();
        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        bearerToken = "Bearer " + auth.token();
    }

    @Test
    void firstPageIsSlicedFromTheFullUnpaginatedSwapiResponse() throws Exception {
        SWAPI.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/films/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(FOUR_FILMS_UNPAGINATED)));

        mockMvc.perform(get("/api/v1/films?page=1&size=2").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("A New Hope"))
                .andExpect(jsonPath("$.content[1].title").value("The Empire Strikes Back"));
    }

    @Test
    void secondPageReturnsTheRemainingFilms() throws Exception {
        SWAPI.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/api/films/"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(FOUR_FILMS_UNPAGINATED)));

        mockMvc.perform(get("/api/v1/films?page=2&size=2").header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Return of the Jedi"))
                .andExpect(jsonPath("$.content[1].title").value("The Phantom Menace"));
    }
}
