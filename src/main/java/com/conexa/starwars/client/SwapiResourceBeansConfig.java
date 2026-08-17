package com.conexa.starwars.client;

import com.conexa.starwars.client.dto.FilmProperties;
import com.conexa.starwars.client.dto.PersonProperties;
import com.conexa.starwars.client.dto.StarshipProperties;
import com.conexa.starwars.client.dto.VehicleProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

// one SwapiResourceClient bean per resource, only the definition and the properties type change
@Configuration
public class SwapiResourceBeansConfig {

    @Bean
    public SwapiResourceClient<PersonProperties> personSwapiResourceClient(
            RestClient swapiRestClient, ObjectMapper objectMapper, SwapiProperties properties) {
        return new SwapiResourceClient<>(swapiRestClient, objectMapper, SwapiResourceDefinition.PEOPLE,
                PersonProperties.class, properties);
    }

    @Bean
    public SwapiResourceClient<FilmProperties> filmSwapiResourceClient(
            RestClient swapiRestClient, ObjectMapper objectMapper, SwapiProperties properties) {
        return new SwapiResourceClient<>(swapiRestClient, objectMapper, SwapiResourceDefinition.FILMS,
                FilmProperties.class, properties);
    }

    @Bean
    public SwapiResourceClient<StarshipProperties> starshipSwapiResourceClient(
            RestClient swapiRestClient, ObjectMapper objectMapper, SwapiProperties properties) {
        return new SwapiResourceClient<>(swapiRestClient, objectMapper, SwapiResourceDefinition.STARSHIPS,
                StarshipProperties.class, properties);
    }

    @Bean
    public SwapiResourceClient<VehicleProperties> vehicleSwapiResourceClient(
            RestClient swapiRestClient, ObjectMapper objectMapper, SwapiProperties properties) {
        return new SwapiResourceClient<>(swapiRestClient, objectMapper, SwapiResourceDefinition.VEHICLES,
                VehicleProperties.class, properties);
    }
}
