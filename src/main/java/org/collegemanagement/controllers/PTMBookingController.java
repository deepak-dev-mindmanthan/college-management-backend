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
import org.collegemanagement.dto.ptm.CreatePTMBookingRequest;
import org.collegemanagement.dto.ptm.PTMBookingResponse;
import org.collegemanagement.services.PTMBookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/ptm-bookings")
@AllArgsConstructor
@Tag(name = "PTM Booking Management", description = "APIs for managing Parent-Teacher Meeting bookings")
public class PTMBookingController {

    private final PTMBookingService ptmBookingService;

    @Operation(
            summary = "Create a PTM booking",
            description = "Creates a new PTM booking. Only parents can book slots for their children. Requires PARENT, COLLEGE_ADMIN, or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PTM booking created successfully",
                    content = @Content(schema = @Schema(implementation = PTMBookingResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Slot or student not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Slot already booked or parent not associated with student"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public ResponseEntity<ApiResponse<PTMBookingResponse>> createPTMBooking(
            @Valid @RequestBody CreatePTMBookingRequest request
    ) {
        PTMBookingResponse booking = ptmBookingService.createPTMBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(booking, "PTM booking created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Cancel a PTM booking",
            description = "Cancels a PTM booking. Only the parent who made the booking or admins can cancel. Requires PARENT, COLLEGE_ADMIN, or SUPER_ADMIN role."
    )
    @DeleteMapping("/{bookingUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public ResponseEntity<ApiResponse<Void>> cancelPTMBooking(
            @Parameter(description = "UUID of the PTM booking to cancel")
            @PathVariable String bookingUuid
    ) {
        ptmBookingService.cancelPTMBooking(bookingUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "PTM booking cancelled successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get PTM booking by UUID",
            description = "Retrieves a PTM booking by UUID. Accessible by appropriate roles."
    )
    @GetMapping("/{bookingUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResponse<PTMBookingResponse>> getPTMBooking(
            @Parameter(description = "UUID of the PTM booking")
            @PathVariable String bookingUuid
    ) {
        PTMBookingResponse booking = ptmBookingService.getPTMBookingByUuid(bookingUuid);
        return ResponseEntity.ok(ApiResponse.success(booking, "PTM booking retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all PTM bookings",
            description = "Retrieves all PTM bookings with pagination. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<PTMBookingResponse>>> getAllPTMBookings(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PTMBookingResponse> bookings = ptmBookingService.getAllPTMBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "PTM bookings retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get PTM bookings by parent",
            description = "Retrieves all PTM bookings for a specific parent. Parents can only view their own bookings. Requires PARENT, COLLEGE_ADMIN, or SUPER_ADMIN role."
    )
    @GetMapping("/parent/{parentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<PTMBookingResponse>>> getPTMBookingsByParent(
            @Parameter(description = "UUID of the parent")
            @PathVariable String parentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PTMBookingResponse> bookings = ptmBookingService.getPTMBookingsByParent(parentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "PTM bookings retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get PTM bookings by student",
            description = "Retrieves all PTM bookings for a specific student. Accessible by appropriate roles."
    )
    @GetMapping("/student/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<PTMBookingResponse>>> getPTMBookingsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PTMBookingResponse> bookings = ptmBookingService.getPTMBookingsByStudent(studentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "PTM bookings retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get PTM bookings by teacher",
            description = "Retrieves all PTM bookings for a specific teacher. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/teacher/{teacherUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<PTMBookingResponse>>> getPTMBookingsByTeacher(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PTMBookingResponse> bookings = ptmBookingService.getPTMBookingsByTeacher(teacherUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "PTM bookings retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get PTM bookings by date",
            description = "Retrieves all PTM bookings for a specific date. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<PTMBookingResponse>>> getPTMBookingsByDate(
            @Parameter(description = "Date (yyyy-MM-dd)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PTMBookingResponse> bookings = ptmBookingService.getPTMBookingsByDate(date, pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "PTM bookings retrieved successfully",HttpStatus.OK.value()));
    }
}

