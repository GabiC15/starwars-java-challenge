package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FilmProperties(
        String title,
        @JsonProperty("episode_id") Integer episodeId,
        @JsonProperty("opening_crawl") String openingCrawl,
        String director,
        String producer,
        @JsonProperty("release_date") String releaseDate,
        List<String> species,
        List<String> starships,
        List<String> vehicles,
        List<String> characters,
        List<String> planets,
        String url
) {
}
