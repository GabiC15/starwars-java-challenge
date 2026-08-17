package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.common.PageResponse;

import java.util.List;

/**
 * Base class for people/films/starships/vehicles services. Filters by id first,
 * then by name, and falls back to a plain listing if neither is given. Every
 * resource service just extends this and provides its own map().
 */
public abstract class AbstractResourceService<P, R> {

    protected final SwapiResourceClient<P> resourceService;

    protected AbstractResourceService(SwapiResourceClient<P> resourceService) {
        this.resourceService = resourceService;
    }

    protected abstract R map(SwapiItem<P> item);

    /** id beats name, name beats a plain list. id also skips page/size. */
    public PageResponse<R> find(String id, String name, int page, int size) {
        if (id != null && !id.isBlank()) {
            R single = map(resourceService.findById(id.trim()));
            return PageResponse.of(List.of(single), 1, 1);
        }
        if (name != null && !name.isBlank()) {
            return resourceService.searchByName(name.trim(), page, size).map(this::map);
        }
        return resourceService.list(page, size).map(this::map);
    }
}
