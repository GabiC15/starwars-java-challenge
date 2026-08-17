package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// what swapi sends back for search (?name=/?title=) and for the films list, no paging fields, just "result"
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiSearchEnvelope<T>(String message, List<T> result) {
}
