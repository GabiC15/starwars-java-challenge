package com.conexa.starwars.service;

import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.common.PageResponse;

import java.util.List;

// filters by id, then by name, then falls back to a plain listing. Same rule for people, films, starships, vehicles
public abstract class AbstractResourceService<P, R> {

    protected final SwapiResourceClient<P> resourceService;

    protected AbstractResourceService(SwapiResourceClient<P> resourceService) {
        this.resourceService = resourceService;
    }

    protected abstract R map(SwapiItem<P> item);

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
