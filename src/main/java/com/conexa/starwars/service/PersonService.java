package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.client.dto.PersonProperties;
import com.conexa.starwars.dto.PersonResponse;
import org.springframework.stereotype.Service;

@Service
public class PersonService extends AbstractResourceService<PersonProperties, PersonResponse> {

    public PersonService(SwapiResourceClient<PersonProperties> personSwapiResourceClient) {
        super(personSwapiResourceClient);
    }

    @Override
    protected PersonResponse map(SwapiItem<PersonProperties> item) {
        return PersonResponse.from(item);
    }
}
