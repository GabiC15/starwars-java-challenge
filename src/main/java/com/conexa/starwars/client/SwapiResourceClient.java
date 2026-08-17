package com.conexa.starwars.client;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.common.exception.ExternalApiException;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.client.dto.SwapiDetailEnvelope;
import com.conexa.starwars.client.dto.SwapiExpandedItem;
import com.conexa.starwars.client.dto.SwapiListEnvelope;
import com.conexa.starwars.client.dto.SwapiSearchEnvelope;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Talks to Swapi for one resource type. Reused for people, films, starships
 *  and vehicles, only the definition and the properties type change.
 */
public class SwapiResourceClient<P> {

    private static final String RESOURCE_PATH = "/{resource}/";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SwapiResourceDefinition definition;
    private final Class<P> propertiesType;
    private final Cache<String, PageResponse<SwapiItem<P>>> pageCache;
    private final Cache<String, List<SwapiItem<P>>> listCache;
    private final Cache<String, SwapiItem<P>> detailCache;

    public SwapiResourceClient(RestClient restClient,
                                 ObjectMapper objectMapper,
                                 SwapiResourceDefinition definition,
                                 Class<P> propertiesType,
                                 SwapiProperties properties) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.definition = definition;
        this.propertiesType = propertiesType;
        long ttlMillis = properties.cacheTtl().toMillis();
        this.pageCache = Caffeine.newBuilder()
                .expireAfterWrite(ttlMillis, TimeUnit.MILLISECONDS)
                .maximumSize(200)
                .build();
        this.listCache = Caffeine.newBuilder()
                .expireAfterWrite(ttlMillis, TimeUnit.MILLISECONDS)
                .maximumSize(100)
                .build();
        this.detailCache = Caffeine.newBuilder()
                .expireAfterWrite(ttlMillis, TimeUnit.MILLISECONDS)
                .maximumSize(500)
                .build();
    }

    /**
     * Plain paginated list, no filter.
     * Must go on a method called from outside the class, or the circuit breaker proxy never sees the call.
     */
    @CircuitBreaker(name = "swapi")
    public PageResponse<SwapiItem<P>> list(int page, int size) {
        if (definition.nativelyPaged()) {
            String key = "list:%d:%d".formatted(page, size);
            return pageCache.get(key, k -> fetchNativePage(page, size));
        }
        // fetch the full list once and slice it in memory below
        List<SwapiItem<P>> all = listCache.get("list:all", k -> fetchAllExpanded());
        return PageResponse.of(all, page, size);
    }

    /** Search by name. Swapi never paginates search results itself. */
    @CircuitBreaker(name = "swapi")
    public PageResponse<SwapiItem<P>> searchByName(String query, int page, int size) {
        // cache the full result set per query, slice it in memory below
        String key = "search:%s".formatted(query.toLowerCase());
        List<SwapiItem<P>> results = listCache.get(key, k -> fetchSearch(query));
        return PageResponse.of(results, page, size);
    }

    /** Single item by id. Throws ResourceNotFoundException if Swapi says 404. */
    @CircuitBreaker(name = "swapi")
    public SwapiItem<P> findById(String id) {
        SwapiItem<P> cached = detailCache.getIfPresent(id);
        if (cached != null) {
            return cached;
        }
        SwapiItem<P> item = fetchDetail(id);
        detailCache.put(id, item);
        return item;
    }

    private PageResponse<SwapiItem<P>> fetchNativePage(int page, int size) {
        String json = fetchRaw(uriBuilder -> uriBuilder.path(RESOURCE_PATH)
                .queryParam("page", page)
                .queryParam("limit", size)
                .queryParam("expanded", true)
                .build(definition.path()));

        SwapiListEnvelope<SwapiExpandedItem<P>> envelope = readValue(json, envelopeType(SwapiListEnvelope.class));
        // Swapi re-serves the last page instead of empty when page is out of range
        List<SwapiItem<P>> items = page > envelope.totalPages()
                ? List.of()
                : envelope.results().stream().map(SwapiItem::from).toList();
        return new PageResponse<>(items, page, size, envelope.totalRecords(), envelope.totalPages());
    }

    private List<SwapiItem<P>> fetchAllExpanded() {
        String json = fetchRaw(uriBuilder -> uriBuilder.path(RESOURCE_PATH).build(definition.path()));
        SwapiSearchEnvelope<SwapiExpandedItem<P>> envelope = readValue(json, envelopeType(SwapiSearchEnvelope.class));
        return envelope.result().stream().map(SwapiItem::from).toList();
    }

    private List<SwapiItem<P>> fetchSearch(String query) {
        String json = fetchRaw(uriBuilder -> uriBuilder.path(RESOURCE_PATH)
                .queryParam(definition.searchParam(), query)
                .build(definition.path()));

        SwapiSearchEnvelope<SwapiExpandedItem<P>> envelope = readValue(json, envelopeType(SwapiSearchEnvelope.class));
        return envelope.result().stream().map(SwapiItem::from).toList();
    }

    private SwapiItem<P> fetchDetail(String id) {
        String json;
        try {
            json = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{resource}/{id}/").build(definition.path(), id))
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(definition.singularLabel() + " with id " + id + " not found");
        } catch (RestClientException e) {
            throw new ExternalApiException("Failed to fetch " + definition.singularLabel() + " " + id + " from SWAPI", e);
        }
        SwapiDetailEnvelope<SwapiExpandedItem<P>> envelope = readValue(json, envelopeType(SwapiDetailEnvelope.class));
        return SwapiItem.from(envelope.result());
    }

    private String fetchRaw(Function<UriBuilder, URI> uriFn) {
        try {
            return restClient.get().uri(uriFn).retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new ExternalApiException("Failed to reach SWAPI for resource " + definition.path(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readValue(String json, JavaType type) {
        try {
            return (T) objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to parse SWAPI response for " + definition.path(), e);
        }
    }

    private JavaType itemType() {
        return objectMapper.getTypeFactory().constructParametricType(SwapiExpandedItem.class, propertiesType);
    }

    private JavaType envelopeType(Class<?> envelopeClass) {
        return objectMapper.getTypeFactory().constructParametricType(envelopeClass, itemType());
    }
}
