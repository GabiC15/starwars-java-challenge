package com.conexa.starwars.controller;

import com.conexa.starwars.common.PageResponse;
import com.conexa.starwars.dto.VehicleResponse;
import com.conexa.starwars.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Vehicle listing and lookup. */
@RestController
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Star Wars vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/api/v1/vehicles")
    @Operation(summary = "List vehicles", description = "Paginated list of Star Wars vehicles, optionally filtered by id and/or name.")
    public PageResponse<VehicleResponse> list(
            @Parameter(description = "When present, name/page/size are ignored and a single-item page is returned.")
            @RequestParam(required = false) String id,
            @Parameter(description = "Case-insensitive partial match on the vehicle's name.")
            @RequestParam(required = false) String name,
            @Parameter(description = "1-based page number.") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size.") @RequestParam(defaultValue = "10") int size
    ) {
        return vehicleService.find(id, name, page, size);
    }
}
