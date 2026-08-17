package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// height/mass/birth_year stay as String, not int. Swapi returns unknown or n/a for some people
@JsonIgnoreProperties(ignoreUnknown = true)
public record PersonProperties(
        String name,
        @JsonProperty("birth_year") String birthYear,
        String gender,
        @JsonProperty("eye_color") String eyeColor,
        @JsonProperty("hair_color") String hairColor,
        @JsonProperty("skin_color") String skinColor,
        String height,
        String mass,
        String homeworld,
        List<String> films,
        List<String> species,
        List<String> starships,
        List<String> vehicles,
        String url
) {
}
