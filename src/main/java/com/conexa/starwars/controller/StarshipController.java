package com.conexa.starwars.controller;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.dto.StarshipResponse;
import com.conexa.starwars.service.StarshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Starships", description = "Star Wars starships")
public class StarshipController {

    private final StarshipService starshipService;

    public StarshipController(StarshipService starshipService) {
        this.starshipService = starshipService;
    }

    @GetMapping("/api/v1/starships")
    @Operation(summary = "List starships", description = "Paginated list of Star Wars starships, optionally filtered by id and/or name.")
    public PageResponse<StarshipResponse> list(
            @Parameter(description = "When present, name/page/size are ignored and a single-item page is returned.")
            @RequestParam(required = false) String id,
            @Parameter(description = "Case-insensitive partial match on the starship's name.")
            @RequestParam(required = false) String name,
            @Parameter(description = "1-based page number.") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size.") @RequestParam(defaultValue = "10") int size
    ) {
        return starshipService.find(id, name, page, size);
    }
}
