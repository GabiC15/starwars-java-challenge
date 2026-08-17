package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// what swapi returns for GET /people/1/
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiDetailEnvelope<T>(String message, T result) {
}
