package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.client.dto.StarshipProperties;
import com.conexa.starwars.dto.StarshipResponse;
import org.springframework.stereotype.Service;

@Service
public class StarshipService extends AbstractResourceService<StarshipProperties, StarshipResponse> {

    public StarshipService(SwapiResourceClient<StarshipProperties> starshipSwapiResourceClient) {
        super(starshipSwapiResourceClient);
    }

    @Override
    protected StarshipResponse map(SwapiItem<StarshipProperties> item) {
        return StarshipResponse.from(item);
    }
}
