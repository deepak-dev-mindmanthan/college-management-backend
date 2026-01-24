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
import org.collegemanagement.dto.hostel.CreateHostelWardenRequest;
import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.dto.hostel.HostelWardenResponse;
import org.collegemanagement.dto.hostel.UpdateHostelWardenRequest;
import org.collegemanagement.services.HostelWardenService;
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
@RequestMapping("/api/v1/hostel-wardens")
@AllArgsConstructor
@Tag(name = "Hostel Warden Management", description = "APIs for managing hostel wardens in the college management system")
public class HostelWardenController {

    private final HostelWardenService hostelWardenService;

    @Operation(
            summary = "Create a new hostel warden",
            description = "Creates a new hostel warden with staff profile. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hostel warden created successfully",
                    content = @Content(schema = @Schema(implementation = HostelWardenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hostel warden with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelWardenResponse>> createHostelWarden(
            @Valid @RequestBody CreateHostelWardenRequest request
    ) {
        HostelWardenResponse warden = hostelWardenService.createHostelWarden(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(warden, "Hostel warden created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update hostel warden information",
            description = "Updates hostel warden details including staff profile. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @PutMapping("/{wardenUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelWardenResponse>> updateHostelWarden(
            @Parameter(description = "UUID of the hostel warden to update")
            @PathVariable String wardenUuid,
            @Valid @RequestBody UpdateHostelWardenRequest request
    ) {
        HostelWardenResponse warden = hostelWardenService.updateHostelWarden(wardenUuid, request);
        return ResponseEntity.ok(ApiResponse.success(warden, "Hostel warden updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostel warden by UUID",
            description = "Retrieves hostel warden information by UUID including assigned hostels. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or the warden themselves."
    )
    @GetMapping("/{wardenUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'HOSTEL_WARDEN')")
    public ResponseEntity<ApiResponse<HostelWardenResponse>> getHostelWarden(
            @Parameter(description = "UUID of the hostel warden")
            @PathVariable String wardenUuid
    ) {
        HostelWardenResponse warden = hostelWardenService.getHostelWardenByUuid(wardenUuid);
        return ResponseEntity.ok(ApiResponse.success(warden, "Hostel warden retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel wardens",
            description = "Retrieves a paginated list of all hostel wardens in the college. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Page<HostelWardenResponse>>> getAllHostelWardens(
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
        Page<HostelWardenResponse> wardens = hostelWardenService.getAllHostelWardens(pageable);
        return ResponseEntity.ok(ApiResponse.success(wardens, "Hostel wardens retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search hostel wardens",
            description = "Searches hostel wardens by name or email. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Page<HostelWardenResponse>>> searchHostelWardens(
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
        Page<HostelWardenResponse> wardens = hostelWardenService.searchHostelWardens(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(wardens, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostels assigned to a warden",
            description = "Retrieves all hostels assigned to a specific warden. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or the warden themselves."
    )
    @GetMapping("/{wardenUuid}/hostels")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'HOSTEL_WARDEN')")
    public ResponseEntity<ApiResponse<List<HostelResponse>>> getHostelsByWarden(
            @Parameter(description = "UUID of the hostel warden")
            @PathVariable String wardenUuid
    ) {
        List<HostelResponse> hostels = hostelWardenService.getHostelsByWarden(wardenUuid);
        return ResponseEntity.ok(ApiResponse.success(hostels, "Hostels retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete hostel warden",
            description = "Deletes a hostel warden. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role. Warden must not be assigned to any hostels."
    )
    @DeleteMapping("/{wardenUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHostelWarden(
            @Parameter(description = "UUID of the hostel warden to delete")
            @PathVariable String wardenUuid
    ) {
        hostelWardenService.deleteHostelWarden(wardenUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Hostel warden deleted successfully",HttpStatus.OK.value()));
    }
}

