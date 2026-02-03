package org.collegemanagement.controllers.admin;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.admission.AdmissionResponse;
import org.collegemanagement.services.admin.AdminAdmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/admissions")
@RequiredArgsConstructor
@Tag(
        name = "Admin Admission Management",
        description = "SUPER_ADMIN APIs to manage Admission across all colleges"
)
public class AdminAdmissionController {

    private final AdminAdmissionService adminAdmissionService;


    @Operation(
            summary = "Get all admission applications",
            description = "Retrieves a paginated list of all admission applications. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
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
        Page<AdmissionResponse> admissions = adminAdmissionService.getAllAdmissions(pageable);
        return ResponseEntity.ok(ApiResponse.success(admissions, "Admission applications retrieved successfully", HttpStatus.OK.value()));
    }
}
