package com.conexa.starwars.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// item shape once "properties" shows up, same whether it's a list, a search hit, or a single detail fetch
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiExpandedItem<P>(String uid, String description, P properties) {
}
