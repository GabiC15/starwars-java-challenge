package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// what swapi returns for GET /people/?page=&limit=
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiListEnvelope<T>(
        String message,
        @JsonProperty("total_records") long totalRecords,
        @JsonProperty("total_pages") int totalPages,
        String previous,
        String next,
        List<T> results
) {
}
