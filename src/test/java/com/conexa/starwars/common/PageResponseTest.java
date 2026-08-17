package com.conexa.starwars.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void splitsContentIntoThePageAsked() {
        List<Integer> all = List.of(1, 2, 3, 4, 5, 6, 7);

        PageResponse<Integer> page = PageResponse.of(all, 2, 3);

        assertThat(page.content()).containsExactly(4, 5, 6);
        assertThat(page.totalElements()).isEqualTo(7);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void aPageBeyondTheLastOneIsEmptyButKeepsTheTotals() {
        List<Integer> all = List.of(1, 2, 3);

        PageResponse<Integer> page = PageResponse.of(all, 5, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void pageAndSizeBelowOneAreClampedToOne() {
        List<Integer> all = List.of(1, 2, 3);

        PageResponse<Integer> page = PageResponse.of(all, 0, -5);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.content()).containsExactly(1);
    }

    @Test
    void mapTransformsContentWithoutTouchingPaginationMetadata() {
        PageResponse<Integer> page = PageResponse.of(List.of(1, 2, 3), 1, 2);

        PageResponse<String> mapped = page.map(i -> "n" + i);

        assertThat(mapped.content()).containsExactly("n1", "n2");
        assertThat(mapped.totalElements()).isEqualTo(3);
        assertThat(mapped.totalPages()).isEqualTo(2);
    }
}
