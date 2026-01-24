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
import org.collegemanagement.dto.discipline.CreateDisciplinaryCaseRequest;
import org.collegemanagement.dto.discipline.DisciplinaryCaseResponse;
import org.collegemanagement.dto.discipline.UpdateDisciplinaryCaseRequest;
import org.collegemanagement.enums.DisciplinaryStatus;
import org.collegemanagement.services.DisciplinaryService;
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
@RequestMapping("/api/v1/disciplinary-cases")
@AllArgsConstructor
@Tag(name = "Disciplinary Case Management", description = "APIs for managing student disciplinary cases")
public class DisciplinaryController {

    private final DisciplinaryService disciplinaryService;

    @Operation(
            summary = "Create a disciplinary case",
            description = "Creates a new disciplinary case for a student. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Disciplinary case created successfully",
                    content = @Content(schema = @Schema(implementation = DisciplinaryCaseResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<DisciplinaryCaseResponse>> createDisciplinaryCase(
            @Valid @RequestBody CreateDisciplinaryCaseRequest request
    ) {
        DisciplinaryCaseResponse caseResponse = disciplinaryService.createDisciplinaryCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(caseResponse, "Disciplinary case created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update disciplinary case",
            description = "Updates a disciplinary case (status, action taken, etc.). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{caseUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<DisciplinaryCaseResponse>> updateDisciplinaryCase(
            @Parameter(description = "UUID of the disciplinary case to update")
            @PathVariable String caseUuid,
            @Valid @RequestBody UpdateDisciplinaryCaseRequest request
    ) {
        DisciplinaryCaseResponse caseResponse = disciplinaryService.updateDisciplinaryCase(caseUuid, request);
        return ResponseEntity.ok(ApiResponse.success(caseResponse, "Disciplinary case updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get disciplinary case by UUID",
            description = "Retrieves a disciplinary case by UUID. Accessible by appropriate roles."
    )
    @GetMapping("/{caseUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<DisciplinaryCaseResponse>> getDisciplinaryCase(
            @Parameter(description = "UUID of the disciplinary case")
            @PathVariable String caseUuid
    ) {
        DisciplinaryCaseResponse caseResponse = disciplinaryService.getDisciplinaryCaseByUuid(caseUuid);
        return ResponseEntity.ok(ApiResponse.success(caseResponse, "Disciplinary case retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all disciplinary cases",
            description = "Retrieves all disciplinary cases with pagination. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryCaseResponse>>> getAllDisciplinaryCases(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "incidentDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DisciplinaryCaseResponse> cases = disciplinaryService.getAllDisciplinaryCases(pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Disciplinary cases retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get disciplinary cases by student",
            description = "Retrieves all disciplinary cases for a specific student. Accessible by appropriate roles."
    )
    @GetMapping("/student/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryCaseResponse>>> getDisciplinaryCasesByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "incidentDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DisciplinaryCaseResponse> cases = disciplinaryService.getDisciplinaryCasesByStudent(studentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Disciplinary cases retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get disciplinary cases by status",
            description = "Retrieves disciplinary cases filtered by status. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryCaseResponse>>> getDisciplinaryCasesByStatus(
            @Parameter(description = "Disciplinary status")
            @PathVariable DisciplinaryStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "incidentDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DisciplinaryCaseResponse> cases = disciplinaryService.getDisciplinaryCasesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Disciplinary cases retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get disciplinary cases by student and status",
            description = "Retrieves disciplinary cases for a student filtered by status. Accessible by appropriate roles."
    )
    @GetMapping("/student/{studentUuid}/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryCaseResponse>>> getDisciplinaryCasesByStudentAndStatus(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Disciplinary status")
            @PathVariable DisciplinaryStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "incidentDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DisciplinaryCaseResponse> cases = disciplinaryService.getDisciplinaryCasesByStudentAndStatus(
                studentUuid, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Disciplinary cases retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get disciplinary cases by date range",
            description = "Retrieves disciplinary cases within a date range. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<DisciplinaryCaseResponse>>> getDisciplinaryCasesByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "incidentDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DisciplinaryCaseResponse> cases = disciplinaryService.getDisciplinaryCasesByDateRange(
                startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Disciplinary cases retrieved successfully",HttpStatus.OK.value()));
    }
}

