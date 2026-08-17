package com.conexa.starwars.service;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.client.SwapiItem;
import com.conexa.starwars.client.SwapiResourceClient;
import com.conexa.starwars.client.dto.PersonProperties;
import com.conexa.starwars.dto.PersonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private SwapiResourceClient<PersonProperties> resourceService;

    private PersonService personService;

    private static SwapiItem<PersonProperties> luke() {
        return new SwapiItem<>("1", new PersonProperties("Luke Skywalker", "19BBY", "male", "blue",
                "blond", "fair", "172", "77", "https://swapi.tech/api/planets/1", List.of(), List.of(),
                List.of(), List.of(), "https://swapi.tech/api/people/1"));
    }

    @Test
    void whenIdIsGivenItFetchesByIdAndReturnsASingleItemPage() {
        personService = new PersonService(resourceService);
        when(resourceService.findById("1")).thenReturn(luke());

        PageResponse<PersonResponse> result = personService.find("1", "ignored-when-id-present", 3, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("Luke Skywalker");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(1);
        verifyNoMoreInteractions(resourceService);
    }

    @Test
    void whenOnlyNameIsGivenItDelegatesToSearch() {
        personService = new PersonService(resourceService);
        when(resourceService.searchByName("luke", 1, 10))
                .thenReturn(PageResponse.of(List.of(luke()), 1, 10));

        PageResponse<PersonResponse> result = personService.find(null, "luke", 1, 10);

        assertThat(result.content()).hasSize(1);
        verify(resourceService).searchByName("luke", 1, 10);
    }

    @Test
    void whenNeitherIdNorNameIsGivenItListsPaginated() {
        personService = new PersonService(resourceService);
        when(resourceService.list(2, 5)).thenReturn(PageResponse.of(List.of(luke()), 2, 5));

        PageResponse<PersonResponse> result = personService.find(null, null, 2, 5);

        assertThat(result.page()).isEqualTo(2);
        verify(resourceService).list(2, 5);
    }
}
