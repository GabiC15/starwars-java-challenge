package com.conexa.starwars.controller;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.dto.FilmResponse;
import com.conexa.starwars.service.FilmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Films", description = "Star Wars films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/api/v1/films")
    @Operation(summary = "List films", description = "Paginated list of Star Wars films, optionally filtered by id and/or title.")
    public PageResponse<FilmResponse> list(
            @Parameter(description = "When present, name/page/size are ignored and a single-item page is returned.")
            @RequestParam(required = false) String id,
            @Parameter(description = "Case-insensitive partial match on the film's title.")
            @RequestParam(required = false) String name,
            @Parameter(description = "1-based page number.") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size.") @RequestParam(defaultValue = "10") int size
    ) {
        return filmService.find(id, name, page, size);
    }
}
