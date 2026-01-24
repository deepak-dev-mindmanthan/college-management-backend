package org.collegemanagement.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.accountant.AccountantResponse;
import org.collegemanagement.services.admin.AdminAccountantService;
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
@RequestMapping("/api/v1/admin/accountants")
@RequiredArgsConstructor
@Tag(
        name = "Admin Accountant Management",
        description = "SUPER_ADMIN APIs to manage accountants across all colleges"
)
public class AdminAccountantController {

    private final AdminAccountantService adminAccountantService;

    @Operation(
            summary = "Get accountants across all colleges",
            description = """
                    Returns a paginated list of all accountants across every college tenant.
                    This API is applicable ONLY to SUPER_ADMIN.
                    """
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AccountantResponse>>> getAllAccountantsGlobal(
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

        Page<AccountantResponse> result =
                adminAccountantService.getAllAccountantsAcrossColleges(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(result, "All accountants retrieved successfully", HttpStatus.OK.value())
        );
    }
}
