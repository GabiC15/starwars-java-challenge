package com.conexa.starwars.dto;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.dto.StarshipProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A Star Wars starship")
public record StarshipResponse(
        String id,
        String name,
        String model,
        String starshipClass,
        String manufacturer,
        @Schema(description = "Cost in galactic credits") String costInCredits,
        @Schema(description = "Length in meters") String length,
        String crew,
        String passengers,
        @Schema(description = "Maximum atmospheric speed in km/h") String maxAtmospheringSpeed,
        @Schema(description = "Hyperdrive class rating") String hyperdriveRating,
        @Schema(description = "Megalights per hour") String mglt,
        @Schema(description = "Cargo capacity in kg") String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {

    public static StarshipResponse from(SwapiItem<StarshipProperties> item) {
        StarshipProperties p = item.properties();
        return new StarshipResponse(
                item.id(), p.name(), p.model(), p.starshipClass(), p.manufacturer(), p.costInCredits(),
                p.length(), p.crew(), p.passengers(), p.maxAtmospheringSpeed(), p.hyperdriveRating(),
                p.mglt(), p.cargoCapacity(), p.consumables(), p.films(), p.pilots(), p.url()
        );
    }
}
