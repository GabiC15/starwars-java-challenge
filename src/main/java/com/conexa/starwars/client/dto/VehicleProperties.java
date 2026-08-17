package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleProperties(
        String name,
        String model,
        @JsonProperty("vehicle_class") String vehicleClass,
        String manufacturer,
        String length,
        @JsonProperty("cost_in_credits") String costInCredits,
        String crew,
        String passengers,
        @JsonProperty("max_atmosphering_speed") String maxAtmospheringSpeed,
        @JsonProperty("cargo_capacity") String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {
}
