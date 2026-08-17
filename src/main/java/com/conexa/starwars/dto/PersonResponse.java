package com.conexa.starwars.dto;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.dto.PersonProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A Star Wars character")
public record PersonResponse(
        String id,
        String name,
        @Schema(example = "19BBY") String birthYear,
        String gender,
        String eyeColor,
        String hairColor,
        String skinColor,
        @Schema(description = "Height in centimeters") String height,
        @Schema(description = "Mass in kilograms") String mass,
        String homeworld,
        List<String> films,
        List<String> species,
        List<String> starships,
        List<String> vehicles,
        String url
) {

    public static PersonResponse from(SwapiItem<PersonProperties> item) {
        PersonProperties p = item.properties();
        return new PersonResponse(
                item.id(), p.name(), p.birthYear(), p.gender(), p.eyeColor(), p.hairColor(),
                p.skinColor(), p.height(), p.mass(), p.homeworld(), p.films(), p.species(),
                p.starships(), p.vehicles(), p.url()
        );
    }
}
