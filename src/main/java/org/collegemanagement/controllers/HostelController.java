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
import org.collegemanagement.dto.hostel.CreateHostelRequest;
import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRequest;
import org.collegemanagement.enums.HostelType;
import org.collegemanagement.services.HostelService;
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
@RequestMapping("/api/v1/hostels")
@AllArgsConstructor
@Tag(name = "Hostel Management", description = "APIs for managing hostels in the college management system")
public class HostelController {

    private final HostelService hostelService;

    @Operation(
            summary = "Create a new hostel",
            description = "Creates a new hostel. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hostel created successfully",
                    content = @Content(schema = @Schema(implementation = HostelResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Hostel with name already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelResponse>> createHostel(
            @Valid @RequestBody CreateHostelRequest request
    ) {
        HostelResponse hostel = hostelService.createHostel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(hostel, "Hostel created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update hostel information",
            description = "Updates hostel details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @PutMapping("/{hostelUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelResponse>> updateHostel(
            @Parameter(description = "UUID of the hostel to update")
            @PathVariable String hostelUuid,
            @Valid @RequestBody UpdateHostelRequest request
    ) {
        HostelResponse hostel = hostelService.updateHostel(hostelUuid, request);
        return ResponseEntity.ok(ApiResponse.success(hostel, "Hostel updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostel by UUID",
            description = "Retrieves hostel information by UUID. Accessible by all authenticated users."
    )   
    @GetMapping("/{hostelUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<HostelResponse>> getHostel(
            @Parameter(description = "UUID of the hostel")
            @PathVariable String hostelUuid
    ) {
        HostelResponse hostel = hostelService.getHostelByUuid(hostelUuid);
        return ResponseEntity.ok(ApiResponse.success(hostel, "Hostel retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostels",
            description = "Retrieves a paginated list of all hostels. Accessible by all authenticated users."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<HostelResponse>>> getAllHostels(
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
        Page<HostelResponse> hostels = hostelService.getAllHostels(pageable);
        return ResponseEntity.ok(ApiResponse.success(hostels, "Hostels retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostels (without pagination)",
            description = "Retrieves all hostels as a list. Accessible by all authenticated users."
    )
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<HostelResponse>>> getAllHostelsList() {
        List<HostelResponse> hostels = hostelService.getAllHostels();
        return ResponseEntity.ok(ApiResponse.success(hostels, "Hostels retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostels by type",
            description = "Retrieves hostels filtered by type (BOYS/GIRLS). Accessible by all authenticated users."
    )
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<HostelResponse>>> getHostelsByType(
            @Parameter(description = "Hostel type (BOYS or GIRLS)")
            @PathVariable HostelType type,
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
        Page<HostelResponse> hostels = hostelService.getHostelsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(hostels, "Hostels retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search hostels",
            description = "Searches hostels by name. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Page<HostelResponse>>> searchHostels(
            @Parameter(description = "Search term (hostel name)")
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
        Page<HostelResponse> hostels = hostelService.searchHostels(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(hostels, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete hostel",
            description = "Deletes a hostel. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @DeleteMapping("/{hostelUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHostel(
            @Parameter(description = "UUID of the hostel to delete")
            @PathVariable String hostelUuid
    ) {
        hostelService.deleteHostel(hostelUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Hostel deleted successfully",HttpStatus.OK.value()));
    }
}

