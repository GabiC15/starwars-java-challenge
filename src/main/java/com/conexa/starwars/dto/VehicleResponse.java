package com.conexa.starwars.dto;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.dto.VehicleProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A Star Wars vehicle")
public record VehicleResponse(
        String id,
        String name,
        String model,
        String vehicleClass,
        String manufacturer,
        @Schema(description = "Length in meters") String length,
        @Schema(description = "Cost in galactic credits") String costInCredits,
        String crew,
        String passengers,
        @Schema(description = "Maximum atmospheric speed in km/h") String maxAtmospheringSpeed,
        @Schema(description = "Cargo capacity in kg") String cargoCapacity,
        String consumables,
        List<String> films,
        List<String> pilots,
        String url
) {

    public static VehicleResponse from(SwapiItem<VehicleProperties> item) {
        VehicleProperties p = item.properties();
        return new VehicleResponse(
                item.id(), p.name(), p.model(), p.vehicleClass(), p.manufacturer(), p.length(),
                p.costInCredits(), p.crew(), p.passengers(), p.maxAtmospheringSpeed(),
                p.cargoCapacity(), p.consumables(), p.films(), p.pilots(), p.url()
        );
    }
}
