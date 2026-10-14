package com.eyeshop.common.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standard paginated response wrapper.
 *
 * Usage:
 *   - Simple mapping:   PageResponse.from(page, mapper)
 *   - Custom mapping:   PageResponse.of(mappedList, page)
 */
public record PageResponse<T>(
    List<T> content,
    int page,               // 0-based
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious,
    String sort,
    List<SortOrder> sortOrders
) {

    public record SortOrder(String field, String direction) {}

    /** Map a Page<T> directly (entity == DTO type). */
    public static <T> PageResponse<T> from(Page<T> page) {
        return of(page.getContent(), page);
    }

    /** Map a Page<E> to PageResponse<T> using a mapper function. */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        List<T> mapped = page.getContent().stream()
            .map(mapper)
            .toList();
        return of(mapped, page);
    }

    /**
     * Wrap a pre-mapped list with metadata from a source page.
     * Use when mapping needs extra data (e.g. batch-fetching related entities).
     */
    public static <T> PageResponse<T> of(List<T> content, Page<?> sourcePage) {
        return new PageResponse<>(
            content,
            sourcePage.getNumber(),
            sourcePage.getSize(),
            sourcePage.getTotalElements(),
            sourcePage.getTotalPages(),
            sourcePage.isFirst(),
            sourcePage.isLast(),
            sourcePage.hasNext(),
            sourcePage.hasPrevious(),
            sourcePage.getSort().toString(),
            sourcePage.getSort().stream()
                .map(o -> new SortOrder(o.getProperty(), o.getDirection().name()))
                .toList()
        );
    }
}