package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.transport.CreateTransportRouteRequest;
import org.collegemanagement.dto.transport.TransportRouteResponse;
import org.collegemanagement.dto.transport.TransportSummaryResponse;
import org.collegemanagement.dto.transport.UpdateTransportRouteRequest;
import org.collegemanagement.services.TransportRouteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transport/routes")
@AllArgsConstructor
@Tag(name = "Transport Route Management", description = "APIs for managing transport routes in the college management system")
public class TransportRouteController {

    private final TransportRouteService transportRouteService;

    @Operation(
            summary = "Create a new transport route",
            description = "Creates a new transport route. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transport route created successfully",
                    content = @Content(schema = @Schema(implementation = TransportRouteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Transport route with name already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportRouteResponse>> createTransportRoute(
            @Valid @RequestBody CreateTransportRouteRequest request
    ) {
        TransportRouteResponse transportRoute = transportRouteService.createTransportRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(transportRoute, "Transport route created successfully", HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update transport route information",
            description = "Updates transport route details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @PutMapping("/{routeUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportRouteResponse>> updateTransportRoute(
            @Parameter(description = "UUID of the transport route to update")
            @PathVariable String routeUuid,
            @Valid @RequestBody UpdateTransportRouteRequest request
    ) {
        TransportRouteResponse transportRoute = transportRouteService.updateTransportRoute(routeUuid, request);
        return ResponseEntity.ok(ApiResponse.success(transportRoute, "Transport route updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport route by UUID",
            description = "Retrieves transport route information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/{routeUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<TransportRouteResponse>> getTransportRoute(
            @Parameter(description = "UUID of the transport route")
            @PathVariable String routeUuid
    ) {
        TransportRouteResponse transportRoute = transportRouteService.getTransportRouteByUuid(routeUuid);
        return ResponseEntity.ok(ApiResponse.success(transportRoute, "Transport route retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all transport routes",
            description = "Retrieves a paginated list of all transport routes. Accessible by all authenticated users."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<TransportRouteResponse>>> getAllTransportRoutes(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "routeName") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TransportRouteResponse> transportRoutes = transportRouteService.getAllTransportRoutes(pageable);
        return ResponseEntity.ok(ApiResponse.success(transportRoutes, "Transport routes retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all transport routes (without pagination)",
            description = "Retrieves all transport routes as a list. Accessible by all authenticated users."
    )
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<TransportRouteResponse>>> getAllTransportRoutesList() {
        List<TransportRouteResponse> transportRoutes = transportRouteService.getAllTransportRoutes();
        return ResponseEntity.ok(ApiResponse.success(transportRoutes, "Transport routes retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search transport routes",
            description = "Searches transport routes by route name, vehicle number, or driver name. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<TransportRouteResponse>>> searchTransportRoutes(
            @Parameter(description = "Search term (route name, vehicle number, or driver name)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "routeName") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TransportRouteResponse> transportRoutes = transportRouteService.searchTransportRoutes(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(transportRoutes, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete transport route",
            description = "Deletes a transport route. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @DeleteMapping("/{routeUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTransportRoute(
            @Parameter(description = "UUID of the transport route to delete")
            @PathVariable String routeUuid
    ) {
        transportRouteService.deleteTransportRoute(routeUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Transport route deleted successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport summary",
            description = "Retrieves transport summary statistics. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportSummaryResponse>> getTransportSummary() {
        TransportSummaryResponse summary = transportRouteService.getTransportSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Transport summary retrieved successfully",HttpStatus.OK.value()));
    }
}

