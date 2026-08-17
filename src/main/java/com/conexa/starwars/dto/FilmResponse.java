package com.conexa.starwars.dto;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.dto.FilmProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A Star Wars film")
public record FilmResponse(
        String id,
        String title,
        Integer episodeId,
        String openingCrawl,
        String director,
        String producer,
        @Schema(example = "1977-05-25") String releaseDate,
        List<String> species,
        List<String> starships,
        List<String> vehicles,
        List<String> characters,
        List<String> planets,
        String url
) {

    public static FilmResponse from(SwapiItem<FilmProperties> item) {
        FilmProperties p = item.properties();
        return new FilmResponse(
                item.id(), p.title(), p.episodeId(), p.openingCrawl(), p.director(), p.producer(),
                p.releaseDate(), p.species(), p.starships(), p.vehicles(), p.characters(), p.planets(), p.url()
        );
    }
}
