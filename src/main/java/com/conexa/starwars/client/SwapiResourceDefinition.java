package com.conexa.starwars.client;

// nativelyPaged: false means swapi ignores page/limit for this resource, so we paginate it ourselves in memory
public record SwapiResourceDefinition(String path, String searchParam, String singularLabel, boolean nativelyPaged) {

    public static final SwapiResourceDefinition PEOPLE =
            new SwapiResourceDefinition("people", "name", "Person", true);
    public static final SwapiResourceDefinition FILMS =
            new SwapiResourceDefinition("films", "title", "Film", false);
    public static final SwapiResourceDefinition STARSHIPS =
            new SwapiResourceDefinition("starships", "name", "Starship", true);
    public static final SwapiResourceDefinition VEHICLES =
            new SwapiResourceDefinition("vehicles", "name", "Vehicle", true);
}
