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
import org.collegemanagement.dto.transport.CreateTransportAllocationRequest;
import org.collegemanagement.dto.transport.TransportAllocationResponse;
import org.collegemanagement.dto.transport.UpdateTransportAllocationRequest;
import org.collegemanagement.services.TransportAllocationService;
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
@RequestMapping("/api/v1/transport/allocations")
@AllArgsConstructor
@Tag(name = "Transport Allocation Management", description = "APIs for managing transport allocations in the college management system")
public class TransportAllocationController {

    private final TransportAllocationService transportAllocationService;

    @Operation(
            summary = "Create a new transport allocation",
            description = "Creates a new transport allocation for a student. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transport allocation created successfully",
                    content = @Content(schema = @Schema(implementation = TransportAllocationResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Student already has an active allocation for this route"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> createTransportAllocation(
            @Valid @RequestBody CreateTransportAllocationRequest request
    ) {
        TransportAllocationResponse allocation = transportAllocationService.createTransportAllocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(allocation, "Transport allocation created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update transport allocation information",
            description = "Updates transport allocation details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @PutMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> updateTransportAllocation(
            @Parameter(description = "UUID of the transport allocation to update")
            @PathVariable String allocationUuid,
            @Valid @RequestBody UpdateTransportAllocationRequest request
    ) {
        TransportAllocationResponse allocation = transportAllocationService.updateTransportAllocation(allocationUuid, request);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Transport allocation updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport allocation by UUID",
            description = "Retrieves transport allocation information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> getTransportAllocation(
            @Parameter(description = "UUID of the transport allocation")
            @PathVariable String allocationUuid
    ) {
        TransportAllocationResponse allocation = transportAllocationService.getTransportAllocationByUuid(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Transport allocation retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all transport allocations",
            description = "Retrieves a paginated list of all transport allocations. Requires COLLEGE_ADMIN, SUPER_ADMIN, TRANSPORT_MANAGER, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<TransportAllocationResponse>>> getAllTransportAllocations(
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
        Page<TransportAllocationResponse> allocations = transportAllocationService.getAllTransportAllocations(pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Transport allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all active transport allocations",
            description = "Retrieves a paginated list of all active transport allocations. Accessible by all authenticated users."
    )
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<TransportAllocationResponse>>> getActiveTransportAllocations(
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
        Page<TransportAllocationResponse> allocations = transportAllocationService.getActiveTransportAllocations(pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Active transport allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport allocations for a student",
            description = "Retrieves all transport allocations for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<TransportAllocationResponse>>> getTransportAllocationsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        List<TransportAllocationResponse> allocations = transportAllocationService.getTransportAllocationsByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Transport allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get active transport allocation for a student",
            description = "Retrieves the active transport allocation for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> getActiveTransportAllocationByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        TransportAllocationResponse allocation = transportAllocationService.getActiveTransportAllocationByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Active transport allocation retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get transport allocations for a route",
            description = "Retrieves a paginated list of all transport allocations for a specific route. Requires COLLEGE_ADMIN, SUPER_ADMIN, TRANSPORT_MANAGER, or TEACHER role."
    )
    @GetMapping("/routes/{routeUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<TransportAllocationResponse>>> getTransportAllocationsByRoute(
            @Parameter(description = "UUID of the transport route")
            @PathVariable String routeUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransportAllocationResponse> allocations = transportAllocationService.getTransportAllocationsByRoute(routeUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Transport allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get active transport allocations for a route",
            description = "Retrieves all active transport allocations for a specific route. Requires COLLEGE_ADMIN, SUPER_ADMIN, TRANSPORT_MANAGER, or TEACHER role."
    )
    @GetMapping("/routes/{routeUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<TransportAllocationResponse>>> getActiveTransportAllocationsByRoute(
            @Parameter(description = "UUID of the transport route")
            @PathVariable String routeUuid
    ) {
        List<TransportAllocationResponse> allocations = transportAllocationService.getActiveTransportAllocationsByRoute(routeUuid);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Active transport allocations retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Release transport allocation",
            description = "Releases/deactivates a transport allocation. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @PutMapping("/{allocationUuid}/release")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<TransportAllocationResponse>> releaseTransportAllocation(
            @Parameter(description = "UUID of the transport allocation to release")
            @PathVariable String allocationUuid
    ) {
        TransportAllocationResponse allocation = transportAllocationService.releaseTransportAllocation(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(allocation, "Transport allocation released successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search transport allocations",
            description = "Searches transport allocations by student name, roll number, route name, or vehicle number. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<TransportAllocationResponse>>> searchTransportAllocations(
            @Parameter(description = "Search term (student name, roll number, route name, or vehicle number)")
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
        Page<TransportAllocationResponse> allocations = transportAllocationService.searchTransportAllocations(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(allocations, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete transport allocation",
            description = "Deletes a transport allocation. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TRANSPORT_MANAGER role."
    )
    @DeleteMapping("/{allocationUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTransportAllocation(
            @Parameter(description = "UUID of the transport allocation to delete")
            @PathVariable String allocationUuid
    ) {
        transportAllocationService.deleteTransportAllocation(allocationUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Transport allocation deleted successfully",HttpStatus.OK.value()));
    }
}

