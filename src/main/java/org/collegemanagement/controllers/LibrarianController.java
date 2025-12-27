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
import org.collegemanagement.dto.librarian.CreateLibrarianRequest;
import org.collegemanagement.dto.librarian.LibrarianResponse;
import org.collegemanagement.dto.librarian.UpdateLibrarianRequest;
import org.collegemanagement.services.LibrarianService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/librarians")
@AllArgsConstructor
@Tag(name = "Librarian Management", description = "APIs for managing librarians in the college management system")
public class LibrarianController {

    private final LibrarianService librarianService;

    @Operation(
            summary = "Create a new librarian",
            description = "Creates a new librarian with staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Librarian created successfully",
                    content = @Content(schema = @Schema(implementation = LibrarianResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Librarian with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<LibrarianResponse>> createLibrarian(
            @Valid @RequestBody CreateLibrarianRequest request
    ) {
        LibrarianResponse librarian = librarianService.createLibrarian(request);
        return ResponseEntity.ok(ApiResponse.success(librarian, "Librarian created successfully"));
    }

    @Operation(
            summary = "Update librarian information",
            description = "Updates librarian details including staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{librarianUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<LibrarianResponse>> updateLibrarian(
            @Parameter(description = "UUID of the librarian to update")
            @PathVariable String librarianUuid,
            @Valid @RequestBody UpdateLibrarianRequest request
    ) {
        LibrarianResponse librarian = librarianService.updateLibrarian(librarianUuid, request);
        return ResponseEntity.ok(ApiResponse.success(librarian, "Librarian updated successfully"));
    }

    @Operation(
            summary = "Get librarian by UUID",
            description = "Retrieves librarian information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the librarian themselves."
    )
    @GetMapping("/{librarianUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LibrarianResponse>> getLibrarian(
            @Parameter(description = "UUID of the librarian")
            @PathVariable String librarianUuid
    ) {
        LibrarianResponse librarian = librarianService.getLibrarianByUuid(librarianUuid);
        return ResponseEntity.ok(ApiResponse.success(librarian, "Librarian retrieved successfully"));
    }

    @Operation(
            summary = "Get all librarians",
            description = "Retrieves a paginated list of all librarians in the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LibrarianResponse>>> getAllLibrarians(
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
        Page<LibrarianResponse> librarians = librarianService.getAllLibrarians(pageable);
        return ResponseEntity.ok(ApiResponse.success(librarians, "Librarians retrieved successfully"));
    }

    @Operation(
            summary = "Search librarians",
            description = "Searches librarians by name or email. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LibrarianResponse>>> searchLibrarians(
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
        Page<LibrarianResponse> librarians = librarianService.searchLibrarians(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(librarians, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Delete librarian",
            description = "Deletes a librarian. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{librarianUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLibrarian(
            @Parameter(description = "UUID of the librarian to delete")
            @PathVariable String librarianUuid
    ) {
        librarianService.deleteLibrarian(librarianUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Librarian deleted successfully"));
    }
}

