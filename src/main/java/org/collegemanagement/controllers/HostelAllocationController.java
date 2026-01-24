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
import org.collegemanagement.dto.hostel.CreateHostelAllocationRequest;
import org.collegemanagement.dto.hostel.HostelAllocationResponse;
import org.collegemanagement.dto.hostel.HostelSummaryResponse;
import org.collegemanagement.dto.hostel.UpdateHostelAllocationRequest;
import org.collegemanagement.services.HostelAllocationService;
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
@RequestMapping("/api/v1/hostel-allocations")
@AllArgsConstructor
@Tag(name = "Hostel Allocation Management", description = "APIs for managing hostel allocations in the college management system")
public class HostelAllocationController {

    private final HostelAllocationService hostelAllocationService;

    @Operation(
            summary = "Create a new hostel allocation",
            description = "Creates a new hostel allocation for a student. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hostel allocation created successfully",
                    content = @Content(schema = @Schema(implementation = HostelAllocationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Student already has an active allocation or room is at full capacity"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> createHostelAllocation(
            @Valid @RequestBody CreateHostelAllocationRequest request
    ) {
        HostelAllocationResponse allocation = hostelAllocationService.createHostelAllocation(request);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Hostel allocation created successfully", HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update hostel allocation information",
            description = "Updates hostel allocation details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @PutMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> updateHostelAllocation(
            @Parameter(description = "UUID of the hostel allocation to update")
            @PathVariable String allocationUuid,
            @Valid @RequestBody UpdateHostelAllocationRequest request
    ) {
        HostelAllocationResponse allocation = hostelAllocationService.updateHostelAllocation(allocationUuid, request);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Hostel allocation updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostel allocation by UUID",
            description = "Retrieves hostel allocation information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> getHostelAllocation(
            @Parameter(description = "UUID of the hostel allocation")
            @PathVariable String allocationUuid
    ) {
        HostelAllocationResponse allocation = hostelAllocationService.getHostelAllocationByUuid(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Hostel allocation retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel allocations",
            description = "Retrieves a paginated list of all hostel allocations. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or TEACHER."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<HostelAllocationResponse>>> getAllHostelAllocations(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "allocatedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelAllocationResponse> allocations = hostelAllocationService.getAllHostelAllocations(pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all active hostel allocations",
            description = "Retrieves a paginated list of all active hostel allocations. Accessible by all authenticated users."
    )
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<HostelAllocationResponse>>> getActiveHostelAllocations(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "allocatedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelAllocationResponse> allocations = hostelAllocationService.getActiveHostelAllocations(pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Active hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel allocations for a student",
            description = "Retrieves all hostel allocations for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/student/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<HostelAllocationResponse>>> getHostelAllocationsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        List<HostelAllocationResponse> allocations = hostelAllocationService.getHostelAllocationsByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get active hostel allocation for a student",
            description = "Retrieves the active hostel allocation for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/student/{studentUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> getActiveHostelAllocationByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        HostelAllocationResponse allocation = hostelAllocationService.getActiveHostelAllocationByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Active hostel allocation retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel allocations for a room",
            description = "Retrieves a paginated list of all allocations for a specific room. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or TEACHER."
    )
    @GetMapping("/room/{roomUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<HostelAllocationResponse>>> getHostelAllocationsByRoom(
            @Parameter(description = "UUID of the room")
            @PathVariable String roomUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "allocatedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelAllocationResponse> allocations = hostelAllocationService.getHostelAllocationsByRoom(roomUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all active hostel allocations for a room",
            description = "Retrieves all active allocations for a specific room. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or TEACHER."
    )
    @GetMapping("/room/{roomUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<HostelAllocationResponse>>> getActiveHostelAllocationsByRoom(
            @Parameter(description = "UUID of the room")
            @PathVariable String roomUuid
    ) {
        List<HostelAllocationResponse> allocations = hostelAllocationService.getActiveHostelAllocationsByRoom(roomUuid);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Active hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel allocations for a hostel",
            description = "Retrieves a paginated list of all allocations for a specific hostel. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or TEACHER."
    )
    @GetMapping("/hostel/{hostelUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<HostelAllocationResponse>>> getHostelAllocationsByHostel(
            @Parameter(description = "UUID of the hostel")
            @PathVariable String hostelUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "allocatedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelAllocationResponse> allocations = hostelAllocationService.getHostelAllocationsByHostel(hostelUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all active hostel allocations for a hostel",
            description = "Retrieves all active allocations for a specific hostel. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, HOSTEL_MANAGER, or TEACHER."
    )
    @GetMapping("/hostel/{hostelUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<HostelAllocationResponse>>> getActiveHostelAllocationsByHostel(
            @Parameter(description = "UUID of the hostel")
            @PathVariable String hostelUuid
    ) {
        List<HostelAllocationResponse> allocations = hostelAllocationService.getActiveHostelAllocationsByHostel(hostelUuid);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Active hostel allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Release hostel allocation",
            description = "Releases/deactivates a hostel allocation. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @PostMapping("/{allocationUuid}/release")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelAllocationResponse>> releaseHostelAllocation(
            @Parameter(description = "UUID of the hostel allocation to release")
            @PathVariable String allocationUuid
    ) {
        HostelAllocationResponse allocation = hostelAllocationService.releaseHostelAllocation(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Hostel allocation released successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search hostel allocations",
            description = "Searches hostel allocations by student name, roll number, room number, or hostel name. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Page<HostelAllocationResponse>>> searchHostelAllocations(
            @Parameter(description = "Search term (student name, roll number, room number, or hostel name)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "allocatedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelAllocationResponse> allocations = hostelAllocationService.searchHostelAllocations(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete hostel allocation",
            description = "Deletes a hostel allocation. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @DeleteMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHostelAllocation(
            @Parameter(description = "UUID of the hostel allocation to delete")
            @PathVariable String allocationUuid
    ) {
        hostelAllocationService.deleteHostelAllocation(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Hostel allocation deleted successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostel summary",
            description = "Retrieves hostel summary statistics. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelSummaryResponse>> getHostelSummary() {
        HostelSummaryResponse summary = hostelAllocationService.getHostelSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Hostel summary retrieved successfully",HttpStatus.OK.value()));
    }
}

