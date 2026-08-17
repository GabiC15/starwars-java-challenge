package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StarshipProperties(
        String name,
        String model,
        @JsonProperty("starship_class") String starshipClass,
        String manufacturer,
        @JsonProperty("cost_in_credits") String costInCredits,
        String length,
        String crew,
        String passengers,
        @JsonProperty("max_atmosphering_speed") String maxAtmospheringSpeed,
        @JsonProperty("hyperdrive_rating") String hyperdriveRating,
        @JsonProperty("MGLT") String mglt,
        @JsonProperty("cargo_capacity") String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {
}
