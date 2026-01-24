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
import org.collegemanagement.dto.transport.CreateTransportManagerRequest;
import org.collegemanagement.dto.transport.TransportManagerResponse;
import org.collegemanagement.dto.transport.UpdateTransportManagerRequest;
import org.collegemanagement.services.TransportManagerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transport-managers")
@AllArgsConstructor
@Tag(name = "Transport Manager Management", description = "APIs for managing transport managers in the college management system")
public class TransportManagerController {

    private final TransportManagerService transportManagerService;

    @Operation(
            summary = "Create a new transport manager",
            description = "Creates a new transport manager with staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transport manager created successfully",
                    content = @Content(schema = @Schema(implementation = TransportManagerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Transport manager with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<TransportManagerResponse>> createTransportManager(
            @Valid @RequestBody CreateTransportManagerRequest request
    ) {
        TransportManagerResponse transportManager = transportManagerService.createTransportManager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(transportManager, "Transport manager created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update transport manager information",
            description = "Updates transport manager details including staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{transportManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<TransportManagerResponse>> updateTransportManager(
            @Parameter(description = "UUID of the transport manager to update")
            @PathVariable String transportManagerUuid,
            @Valid @RequestBody UpdateTransportManagerRequest request
    ) {
        TransportManagerResponse transportManager = transportManagerService.updateTransportManager(transportManagerUuid, request);
        return ResponseEntity.ok(ApiResponse.success(transportManager, "Transport manager updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport manager by UUID",
            description = "Retrieves transport manager information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the transport manager themselves."
    )
    @GetMapping("/{transportManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportManagerResponse>> getTransportManager(
            @Parameter(description = "UUID of the transport manager")
            @PathVariable String transportManagerUuid
    ) {
        TransportManagerResponse transportManager = transportManagerService.getTransportManagerByUuid(transportManagerUuid);
        return ResponseEntity.ok(ApiResponse.success(transportManager, "Transport manager retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all transport managers",
            description = "Retrieves a paginated list of all transport managers in the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransportManagerResponse>>> getAllTransportManagers(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TransportManagerResponse> transportManagers = transportManagerService.getAllTransportManagers(pageable);
        return ResponseEntity.ok(ApiResponse.success(transportManagers, "Transport managers retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search transport managers",
            description = "Searches transport managers by name or email. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransportManagerResponse>>> searchTransportManagers(
            @Parameter(description = "Search term (name or email)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TransportManagerResponse> transportManagers = transportManagerService.searchTransportManagers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(transportManagers, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete transport manager",
            description = "Deletes a transport manager. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{transportManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTransportManager(
            @Parameter(description = "UUID of the transport manager to delete")
            @PathVariable String transportManagerUuid
    ) {
        transportManagerService.deleteTransportManager(transportManagerUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Transport manager deleted successfully",HttpStatus.OK.value()));
    }
}

