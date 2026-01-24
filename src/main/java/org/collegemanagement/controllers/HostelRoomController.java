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
import org.collegemanagement.dto.hostel.CreateHostelRoomRequest;
import org.collegemanagement.dto.hostel.HostelRoomResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRoomRequest;
import org.collegemanagement.services.HostelRoomService;
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
@RequestMapping("/api/v1/hostel-rooms")
@AllArgsConstructor
@Tag(name = "Hostel Room Management", description = "APIs for managing hostel rooms in the college management system")
public class HostelRoomController {

    private final HostelRoomService hostelRoomService;

    @Operation(
            summary = "Create a new hostel room",
            description = "Creates a new hostel room. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hostel room created successfully",
                    content = @Content(schema = @Schema(implementation = HostelRoomResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Room number already exists in this hostel"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelRoomResponse>> createHostelRoom(
            @Valid @RequestBody CreateHostelRoomRequest request
    ) {
        HostelRoomResponse room = hostelRoomService.createHostelRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(room, "Hostel room created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update hostel room information",
            description = "Updates hostel room details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @PutMapping("/{roomUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<HostelRoomResponse>> updateHostelRoom(
            @Parameter(description = "UUID of the hostel room to update")
            @PathVariable String roomUuid,
            @Valid @RequestBody UpdateHostelRoomRequest request
    ) {
        HostelRoomResponse room = hostelRoomService.updateHostelRoom(roomUuid, request);
        return ResponseEntity.ok(ApiResponse.success(room, "Hostel room updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get hostel room by UUID",
            description = "Retrieves hostel room information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/{roomUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<HostelRoomResponse>> getHostelRoom(
            @Parameter(description = "UUID of the hostel room")
            @PathVariable String roomUuid
    ) {
        HostelRoomResponse room = hostelRoomService.getHostelRoomByUuid(roomUuid);
        return ResponseEntity.ok(ApiResponse.success(room, "Hostel room retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel rooms",
            description = "Retrieves a paginated list of all hostel rooms. Accessible by all authenticated users."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<HostelRoomResponse>>> getAllHostelRooms(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "roomNumber") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelRoomResponse> rooms = hostelRoomService.getAllHostelRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(rooms, "Hostel rooms retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel rooms by hostel",
            description = "Retrieves a paginated list of all rooms for a specific hostel. Accessible by all authenticated users."
    )
    @GetMapping("/hostel/{hostelUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<HostelRoomResponse>>> getHostelRoomsByHostel(
            @Parameter(description = "UUID of the hostel")
            @PathVariable String hostelUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "roomNumber") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelRoomResponse> rooms = hostelRoomService.getHostelRoomsByHostel(hostelUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(rooms, "Hostel rooms retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all hostel rooms by hostel (without pagination)",
            description = "Retrieves all rooms for a specific hostel as a list. Accessible by all authenticated users."
    )
    @GetMapping("/hostel/{hostelUuid}/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<HostelRoomResponse>>> getAllHostelRoomsByHostel(
            @Parameter(description = "UUID of the hostel")
            @PathVariable String hostelUuid
    ) {
        List<HostelRoomResponse> rooms = hostelRoomService.getAllHostelRoomsByHostel(hostelUuid);
        return ResponseEntity.ok(ApiResponse.success(rooms, "Hostel rooms retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search hostel rooms",
            description = "Searches hostel rooms by room number or hostel name. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Page<HostelRoomResponse>>> searchHostelRooms(
            @Parameter(description = "Search term (room number or hostel name)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "roomNumber") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<HostelRoomResponse> rooms = hostelRoomService.searchHostelRooms(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(rooms, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete hostel room",
            description = "Deletes a hostel room. Requires COLLEGE_ADMIN, SUPER_ADMIN, or HOSTEL_MANAGER role."
    )
    @DeleteMapping("/{roomUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHostelRoom(
            @Parameter(description = "UUID of the hostel room to delete")
            @PathVariable String roomUuid
    ) {
        hostelRoomService.deleteHostelRoom(roomUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Hostel room deleted successfully",HttpStatus.OK.value()));
    }
}

