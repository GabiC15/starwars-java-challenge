package com.conexa.starwars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI starWarsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Star Wars Challenge API")
                        .version("v1")
                        .description("Paginated, filterable access to People, Films, Starships and Vehicles from "
                                + "SWAPI, behind JWT authentication. Register or log in via /api/v1/auth "
                                + "to get a token, then authorize with it to try the other endpoints."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
