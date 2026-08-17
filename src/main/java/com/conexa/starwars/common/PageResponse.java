package com.conexa.starwars.common;

import java.util.List;
import java.util.function.Function;

// every endpoint returns this same page shape
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> of(List<T> fullContent, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int totalElements = fullContent.size();
        int totalPages = (int) Math.ceil(totalElements / (double) safeSize);

        int fromIndex = (safePage - 1) * safeSize;
        if (fromIndex >= totalElements) {
            return new PageResponse<>(List.of(), safePage, safeSize, totalElements, totalPages);
        }
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        return new PageResponse<>(fullContent.subList(fromIndex, toIndex), safePage, safeSize, totalElements, totalPages);
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
