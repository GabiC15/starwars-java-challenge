package com.conexa.starwars.client;

import com.conexa.starwars.client.dto.SwapiExpandedItem;

// normalized SWAPI item id + properties
public record SwapiItem<P>(String id, P properties) {

    public static <P> SwapiItem<P> from(SwapiExpandedItem<P> raw) {
        return new SwapiItem<>(raw.uid(), raw.properties());
    }
}
