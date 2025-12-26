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
import org.collegemanagement.dto.admission.*;
import org.collegemanagement.dto.student.StudentResponse;
import org.collegemanagement.enums.AdmissionStatus;
import org.collegemanagement.services.AdmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admissions")
@AllArgsConstructor
@Tag(name = "Admission Management", description = "APIs for managing student admission applications")
public class AdmissionController {

    private final AdmissionService admissionService;

    @Operation(
            summary = "Create a new admission application",
            description = "Creates a new admission application in DRAFT status. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admission application created successfully",
                    content = @Content(schema = @Schema(implementation = AdmissionResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> createAdmission(
            @Valid @RequestBody CreateAdmissionRequest request
    ) {
        AdmissionResponse admission = admissionService.createAdmission(request);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application created successfully"));
    }

    @Operation(
            summary = "Update admission application",
            description = "Updates admission application details. Only allowed for DRAFT applications. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{admissionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> updateAdmission(
            @Parameter(description = "UUID of the admission application to update")
            @PathVariable String admissionUuid,
            @Valid @RequestBody UpdateAdmissionRequest request
    ) {
        AdmissionResponse admission = admissionService.updateAdmission(admissionUuid, request);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application updated successfully"));
    }

    @Operation(
            summary = "Get admission application by UUID",
            description = "Retrieves admission application information by UUID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/{admissionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> getAdmission(
            @Parameter(description = "UUID of the admission application")
            @PathVariable String admissionUuid
    ) {
        AdmissionResponse admission = admissionService.getAdmissionByUuid(admissionUuid);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application retrieved successfully"));
    }

    @Operation(
            summary = "Get all admission applications",
            description = "Retrieves a paginated list of all admission applications. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdmissionResponse>>> getAllAdmissions(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AdmissionResponse> admissions = admissionService.getAllAdmissions(pageable);
        return ResponseEntity.ok(ApiResponse.success(admissions, "Admission applications retrieved successfully"));
    }

    @Operation(
            summary = "Search admission applications",
            description = "Searches admission applications by student name, email, phone, or application number. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdmissionResponse>>> searchAdmissions(
            @Parameter(description = "Search term (student name, email, phone, or application number)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdmissionResponse> admissions = admissionService.searchAdmissions(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(admissions, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Get admission applications by status",
            description = "Retrieves admission applications filtered by status. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdmissionResponse>>> getAdmissionsByStatus(
            @Parameter(description = "Admission status")
            @PathVariable AdmissionStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdmissionResponse> admissions = admissionService.getAdmissionsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(admissions, "Admission applications retrieved successfully"));
    }

    @Operation(
            summary = "Get admission applications by class",
            description = "Retrieves all admission applications for a specific class. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/class/{classUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdmissionResponse>>> getAdmissionsByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdmissionResponse> admissions = admissionService.getAdmissionsByClass(classUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(admissions, "Admission applications retrieved successfully"));
    }

    @Operation(
            summary = "Submit admission application",
            description = "Submits an admission application (DRAFT → SUBMITTED). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{admissionUuid}/submit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> submitAdmission(
            @Parameter(description = "UUID of the admission application")
            @PathVariable String admissionUuid
    ) {
        AdmissionResponse admission = admissionService.submitAdmission(admissionUuid);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application submitted successfully"));
    }

    @Operation(
            summary = "Verify admission application",
            description = "Verifies an admission application (SUBMITTED → VERIFIED). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{admissionUuid}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> verifyAdmission(
            @Parameter(description = "UUID of the admission application")
            @PathVariable String admissionUuid
    ) {
        AdmissionResponse admission = admissionService.verifyAdmission(admissionUuid);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application verified successfully"));
    }

    @Operation(
            summary = "Approve admission application",
            description = "Approves an admission application (VERIFIED → APPROVED) and creates a Student record. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{admissionUuid}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> approveAdmission(
            @Parameter(description = "UUID of the admission application")
            @PathVariable String admissionUuid,
            @Valid @RequestBody ApproveAdmissionRequest request
    ) {
        StudentResponse student = admissionService.approveAdmission(admissionUuid, request);
        return ResponseEntity.ok(ApiResponse.success(student, "Admission application approved and student created successfully"));
    }

    @Operation(
            summary = "Reject admission application",
            description = "Rejects an admission application (any status → REJECTED). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{admissionUuid}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionResponse>> rejectAdmission(
            @Parameter(description = "UUID of the admission application")
            @PathVariable String admissionUuid
    ) {
        AdmissionResponse admission = admissionService.rejectAdmission(admissionUuid);
        return ResponseEntity.ok(ApiResponse.success(admission, "Admission application rejected successfully"));
    }

    @Operation(
            summary = "Delete admission application",
            description = "Deletes an admission application. Only allowed for DRAFT applications. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{admissionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAdmission(
            @Parameter(description = "UUID of the admission application to delete")
            @PathVariable String admissionUuid
    ) {
        admissionService.deleteAdmission(admissionUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Admission application deleted successfully"));
    }

    @Operation(
            summary = "Get admission summary",
            description = "Retrieves admission application summary statistics. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AdmissionSummary>> getAdmissionSummary() {
        AdmissionSummary summary = admissionService.getAdmissionSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Admission summary retrieved successfully"));
    }
}

