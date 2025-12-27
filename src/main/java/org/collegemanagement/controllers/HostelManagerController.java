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
import org.collegemanagement.dto.hostel.CreateHostelManagerRequest;
import org.collegemanagement.dto.hostel.HostelManagerResponse;
import org.collegemanagement.dto.hostel.UpdateHostelManagerRequest;
import org.collegemanagement.services.HostelManagerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hostel-managers")
@AllArgsConstructor
@Tag(name = "Hostel Manager Management", description = "APIs for managing hostel managers in the college management system")
public class HostelManagerController {

    private final HostelManagerService hostelManagerService;

    @Operation(
            summary = "Create a new hostel manager",
            description = "Creates a new hostel manager with staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hostel manager created successfully",
                    content = @Content(schema = @Schema(implementation = HostelManagerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hostel manager with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<HostelManagerResponse>> createHostelManager(
            @Valid @RequestBody CreateHostelManagerRequest request
    ) {
        HostelManagerResponse hostelManager = hostelManagerService.createHostelManager(request);
        return ResponseEntity.ok(ApiResponse.success(hostelManager, "Hostel manager created successfully"));
    }

    @Operation(
            summary = "Update hostel manager information",
            description = "Updates hostel manager details including staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{hostelManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<HostelManagerResponse>> updateHostelManager(
            @Parameter(description = "UUID of the hostel manager to update")
            @PathVariable String hostelManagerUuid,
            @Valid @RequestBody UpdateHostelManagerRequest request
    ) {
        HostelManagerResponse hostelManager = hostelManagerService.updateHostelManager(hostelManagerUuid, request);
        return ResponseEntity.ok(ApiResponse.success(hostelManager, "Hostel manager updated successfully"));
    }

    @Operation(
            summary = "Get hostel manager by UUID",
            description = "Retrieves hostel manager information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the hostel manager themselves."
    )
    @GetMapping("/{hostelManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelManagerResponse>> getHostelManager(
            @Parameter(description = "UUID of the hostel manager")
            @PathVariable String hostelManagerUuid
    ) {
        HostelManagerResponse hostelManager = hostelManagerService.getHostelManagerByUuid(hostelManagerUuid);
        return ResponseEntity.ok(ApiResponse.success(hostelManager, "Hostel manager retrieved successfully"));
    }

    @Operation(
            summary = "Get all hostel managers",
            description = "Retrieves a paginated list of all hostel managers in the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<HostelManagerResponse>>> getAllHostelManagers(
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
        Page<HostelManagerResponse> hostelManagers = hostelManagerService.getAllHostelManagers(pageable);
        return ResponseEntity.ok(ApiResponse.success(hostelManagers, "Hostel managers retrieved successfully"));
    }

    @Operation(
            summary = "Search hostel managers",
            description = "Searches hostel managers by name or email. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<HostelManagerResponse>>> searchHostelManagers(
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
        Page<HostelManagerResponse> hostelManagers = hostelManagerService.searchHostelManagers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(hostelManagers, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Delete hostel manager",
            description = "Deletes a hostel manager. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{hostelManagerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHostelManager(
            @Parameter(description = "UUID of the hostel manager to delete")
            @PathVariable String hostelManagerUuid
    ) {
        hostelManagerService.deleteHostelManager(hostelManagerUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Hostel manager deleted successfully"));
    }
}

