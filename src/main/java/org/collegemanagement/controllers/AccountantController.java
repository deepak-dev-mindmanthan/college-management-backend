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
import org.collegemanagement.dto.accountant.AccountantResponse;
import org.collegemanagement.dto.accountant.CreateAccountantRequest;
import org.collegemanagement.dto.accountant.UpdateAccountantRequest;
import org.collegemanagement.services.AccountantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accountants")
@AllArgsConstructor
@Tag(name = "Accountant Management", description = "APIs for managing accountants in the college management system")
public class AccountantController {

    private final AccountantService accountantService;

    @Operation(
            summary = "Create a new accountant",
            description = "Creates a new accountant with staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accountant created successfully",
                    content = @Content(schema = @Schema(implementation = AccountantResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Accountant with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AccountantResponse>> createAccountant(
            @Valid @RequestBody CreateAccountantRequest request
    ) {
        AccountantResponse accountant = accountantService.createAccountant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(accountant, "Accountant created successfully", HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update accountant information",
            description = "Updates accountant details including staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{accountantUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<AccountantResponse>> updateAccountant(
            @Parameter(description = "UUID of the accountant to update")
            @PathVariable String accountantUuid,
            @Valid @RequestBody UpdateAccountantRequest request
    ) {
        AccountantResponse accountant = accountantService.updateAccountant(accountantUuid, request);
        return ResponseEntity.ok(ApiResponse.success(accountant, "Accountant updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get accountant by UUID",
            description = "Retrieves accountant information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the accountant themselves."
    )
    @GetMapping("/{accountantUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<AccountantResponse>> getAccountant(
            @Parameter(description = "UUID of the accountant")
            @PathVariable String accountantUuid
    ) {
        AccountantResponse accountant = accountantService.getAccountantByUuid(accountantUuid);
        return ResponseEntity.ok(ApiResponse.success(accountant, "Accountant retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all accountants",
            description = "Retrieves a paginated list of all accountants in the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AccountantResponse>>> getAllAccountants(
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
        Page<AccountantResponse> accountants = accountantService.getAllAccountants(pageable);
        return ResponseEntity.ok(ApiResponse.success(accountants, "Accountants retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search accountants",
            description = "Searches accountants by name or email. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AccountantResponse>>> searchAccountants(
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
        Page<AccountantResponse> accountants = accountantService.searchAccountants(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(accountants, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete accountant",
            description = "Deletes an accountant. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{accountantUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAccountant(
            @Parameter(description = "UUID of the accountant to delete")
            @PathVariable String accountantUuid
    ) {
        accountantService.deleteAccountant(accountantUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Accountant deleted successfully",HttpStatus.OK.value()));
    }
}

