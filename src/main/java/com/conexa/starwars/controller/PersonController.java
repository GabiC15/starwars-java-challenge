package com.conexa.starwars.controller;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.dto.PersonResponse;
import com.conexa.starwars.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * People endpoints. Same filter rules as films, starships and vehicles:
 * id wins if it's there, then name, otherwise just list everything.
 */
@RestController
@Tag(name = "People", description = "Star Wars characters")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/api/v1/people")
    @Operation(summary = "List people", description = "Paginated list of Star Wars characters, optionally filtered by id and/or name.")
    public PageResponse<PersonResponse> list(
            @Parameter(description = "When present, name/page/size are ignored and a single-item page is returned.")
            @RequestParam(required = false) String id,
            @Parameter(description = "Case-insensitive partial match on the character's name.")
            @RequestParam(required = false) String name,
            @Parameter(description = "1-based page number.") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size.") @RequestParam(defaultValue = "10") int size
    ) {
        return personService.find(id, name, page, size);
    }
}
