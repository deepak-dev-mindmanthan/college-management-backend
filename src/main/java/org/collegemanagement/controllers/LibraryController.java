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
import org.collegemanagement.dto.library.*;
import org.collegemanagement.enums.LibraryIssueStatus;
import org.collegemanagement.services.LibraryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/library")
@AllArgsConstructor
@Tag(name = "Library Management", description = "APIs for managing library books and issues in the college management system")
public class LibraryController {

    private final LibraryService libraryService;

    // ========== Book Management Endpoints ==========

    @Operation(
            summary = "Create a new book",
            description = "Creates a new book in the library. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Book with ISBN already exists"
            )
    })
    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request
    ) {
        BookResponse book = libraryService.createBook(request);
        return ResponseEntity.ok(ApiResponse.success(book, "Book created successfully"));
    }

    @Operation(
            summary = "Update book information",
            description = "Updates book details. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/books/{bookUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @Parameter(description = "UUID of the book to update")
            @PathVariable String bookUuid,
            @Valid @RequestBody UpdateBookRequest request
    ) {
        BookResponse book = libraryService.updateBook(bookUuid, request);
        return ResponseEntity.ok(ApiResponse.success(book, "Book updated successfully"));
    }

    @Operation(
            summary = "Get book by UUID",
            description = "Retrieves book information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/books/{bookUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(
            @Parameter(description = "UUID of the book")
            @PathVariable String bookUuid
    ) {
        BookResponse book = libraryService.getBookByUuid(bookUuid);
        return ResponseEntity.ok(ApiResponse.success(book, "Book retrieved successfully"));
    }

    @Operation(
            summary = "Get all books",
            description = "Retrieves a paginated list of all books in the library. Accessible by all authenticated users."
    )
    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BookResponse> books = libraryService.getAllBooks(pageable);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    @Operation(
            summary = "Search books",
            description = "Searches books by title, author, ISBN, or category. Accessible by all authenticated users."
    )
    @GetMapping("/books/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @Parameter(description = "Search term (title, author, ISBN, or category)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<BookResponse> books = libraryService.searchBooks(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(books, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Get books by category",
            description = "Retrieves books filtered by category. Accessible by all authenticated users."
    )
    @GetMapping("/books/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooksByCategory(
            @Parameter(description = "Book category")
            @PathVariable String category,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> books = libraryService.getBooksByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    @Operation(
            summary = "Get available books",
            description = "Retrieves books that have available copies. Accessible by all authenticated users."
    )
    @GetMapping("/books/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAvailableBooks(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> books = libraryService.getAvailableBooks(pageable);
        return ResponseEntity.ok(ApiResponse.success(books, "Available books retrieved successfully"));
    }

    @Operation(
            summary = "Delete book",
            description = "Deletes a book. Book must not have any active issues. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/books/{bookUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @Parameter(description = "UUID of the book to delete")
            @PathVariable String bookUuid
    ) {
        libraryService.deleteBook(bookUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }

    // ========== Book Issue/Return Endpoints ==========

    @Operation(
            summary = "Issue book to a user",
            description = "Issues a book to a user (student/staff). Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/issues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<LibraryIssueResponse>> issueBook(
            @Valid @RequestBody IssueBookRequest request
    ) {
        LibraryIssueResponse issue = libraryService.issueBook(request);
        return ResponseEntity.ok(ApiResponse.success(issue, "Book issued successfully"));
    }

    @Operation(
            summary = "Return book",
            description = "Returns a borrowed book. Fine will be calculated automatically if overdue. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/issues/{issueUuid}/return")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<LibraryIssueResponse>> returnBook(
            @Parameter(description = "UUID of the issue")
            @PathVariable String issueUuid,
            @RequestBody(required = false) ReturnBookRequest request
    ) {
        if (request == null) {
            request = ReturnBookRequest.builder().build();
        }
        LibraryIssueResponse issue = libraryService.returnBook(issueUuid, request);
        return ResponseEntity.ok(ApiResponse.success(issue, "Book returned successfully"));
    }

    // ========== Issue Management Endpoints ==========

    @Operation(
            summary = "Get issue by UUID",
            description = "Retrieves issue information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/issues/{issueUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<LibraryIssueResponse>> getIssue(
            @Parameter(description = "UUID of the issue")
            @PathVariable String issueUuid
    ) {
        LibraryIssueResponse issue = libraryService.getIssueByUuid(issueUuid);
        return ResponseEntity.ok(ApiResponse.success(issue, "Issue retrieved successfully"));
    }

    @Operation(
            summary = "Get all issues",
            description = "Retrieves a paginated list of all book issues. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/issues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getAllIssues(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<LibraryIssueResponse> issues = libraryService.getAllIssues(pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Issues retrieved successfully"));
    }

    @Operation(
            summary = "Get issues by status",
            description = "Retrieves issues filtered by status. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/issues/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getIssuesByStatus(
            @Parameter(description = "Issue status")
            @PathVariable LibraryIssueStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryIssueResponse> issues = libraryService.getIssuesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Issues retrieved successfully"));
    }

    @Operation(
            summary = "Get issues by user",
            description = "Retrieves all book issues for a specific user. Users can only view their own issues."
    )
    @GetMapping("/users/{userUuid}/issues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getIssuesByUser(
            @Parameter(description = "UUID of the user")
            @PathVariable String userUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryIssueResponse> issues = libraryService.getIssuesByUser(userUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Issues retrieved successfully"));
    }

    @Operation(
            summary = "Get active issues by user",
            description = "Retrieves active (currently issued) books for a specific user. Users can only view their own issues."
    )
    @GetMapping("/users/{userUuid}/issues/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getActiveIssuesByUser(
            @Parameter(description = "UUID of the user")
            @PathVariable String userUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryIssueResponse> issues = libraryService.getActiveIssuesByUser(userUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Active issues retrieved successfully"));
    }

    @Operation(
            summary = "Get overdue issues",
            description = "Retrieves all overdue book issues. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/issues/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getOverdueIssues(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryIssueResponse> issues = libraryService.getOverdueIssues(pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Overdue issues retrieved successfully"));
    }

    @Operation(
            summary = "Get overdue issues by user",
            description = "Retrieves overdue books for a specific user. Users can only view their own overdue issues."
    )
    @GetMapping("/users/{userUuid}/issues/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<LibraryIssueResponse>>> getOverdueIssuesByUser(
            @Parameter(description = "UUID of the user")
            @PathVariable String userUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LibraryIssueResponse> issues = libraryService.getOverdueIssuesByUser(userUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(issues, "Overdue issues retrieved successfully"));
    }

    @Operation(
            summary = "Update issue status",
            description = "Updates the status of a book issue (e.g., mark as OVERDUE). Requires COLLEGE_ADMIN, LIBRARIAN, or SUPER_ADMIN role."
    )
    @PutMapping("/issues/{issueUuid}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LibraryIssueResponse>> updateIssueStatus(
            @Parameter(description = "UUID of the issue")
            @PathVariable String issueUuid,
            @Parameter(description = "New issue status")
            @RequestParam LibraryIssueStatus status
    ) {
        LibraryIssueResponse issue = libraryService.updateIssueStatus(issueUuid, status);
        return ResponseEntity.ok(ApiResponse.success(issue, "Issue status updated successfully"));
    }

    @Operation(
            summary = "Calculate fine for an issue",
            description = "Calculates the fine amount for an overdue book issue. Accessible by all authenticated users."
    )
    @GetMapping("/issues/{issueUuid}/fine")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateFine(
            @Parameter(description = "UUID of the issue")
            @PathVariable String issueUuid
    ) {
        BigDecimal fine = libraryService.calculateFine(issueUuid);
        return ResponseEntity.ok(ApiResponse.success(fine, "Fine calculated successfully"));
    }

    // ========== Library Summary Endpoints ==========

    @Operation(
            summary = "Get library summary",
            description = "Retrieves library summary statistics. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<ApiResponse<LibrarySummary>> getLibrarySummary() {
        LibrarySummary summary = libraryService.getLibrarySummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Library summary retrieved successfully"));
    }
}

