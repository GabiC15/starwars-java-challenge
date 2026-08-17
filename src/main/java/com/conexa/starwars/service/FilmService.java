package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.client.dto.FilmProperties;
import com.conexa.starwars.dto.FilmResponse;
import org.springframework.stereotype.Service;

@Service
public class FilmService extends AbstractResourceService<FilmProperties, FilmResponse> {

    public FilmService(SwapiResourceClient<FilmProperties> filmSwapiResourceClient) {
        super(filmSwapiResourceClient);
    }

    @Override
    protected FilmResponse map(SwapiItem<FilmProperties> item) {
        return FilmResponse.from(item);
    }
}
