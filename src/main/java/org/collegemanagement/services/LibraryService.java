package org.collegemanagement.services;

import org.collegemanagement.dto.library.*;
import org.collegemanagement.enums.LibraryIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibraryService {

    /**
     * Create a new book
     */
    BookResponse createBook(CreateBookRequest request);

    /**
     * Update book information
     */
    BookResponse updateBook(String bookUuid, UpdateBookRequest request);

    /**
     * Get book by UUID
     */
    BookResponse getBookByUuid(String bookUuid);

    /**
     * Get all books with pagination
     */
    Page<BookResponse> getAllBooks(Pageable pageable);

    /**
     * Search books by title, author, ISBN, or category
     */
    Page<BookResponse> searchBooks(String searchTerm, Pageable pageable);

    /**
     * Get books by category
     */
    Page<BookResponse> getBooksByCategory(String category, Pageable pageable);

    /**
     * Get available books (availableCopies > 0)
     */
    Page<BookResponse> getAvailableBooks(Pageable pageable);

    /**
     * Delete book by UUID
     */
    void deleteBook(String bookUuid);

    /**
     * Issue book to a user
     */
    LibraryIssueResponse issueBook(IssueBookRequest request);

    /**
     * Return book
     */
    LibraryIssueResponse returnBook(String issueUuid, ReturnBookRequest request);

    /**
     * Get issue by UUID
     */
    LibraryIssueResponse getIssueByUuid(String issueUuid);

    /**
     * Get all issues with pagination
     */
    Page<LibraryIssueResponse> getAllIssues(Pageable pageable);

    /**
     * Get issues by status
     */
    Page<LibraryIssueResponse> getIssuesByStatus(LibraryIssueStatus status, Pageable pageable);

    /**
     * Get issues by user (borrowed books)
     */
    Page<LibraryIssueResponse> getIssuesByUser(String userUuid, Pageable pageable);

    /**
     * Get active issues (ISSUED status) by user
     */
    Page<LibraryIssueResponse> getActiveIssuesByUser(String userUuid, Pageable pageable);

    /**
     * Get overdue issues
     */
    Page<LibraryIssueResponse> getOverdueIssues(Pageable pageable);

    /**
     * Get overdue issues by user
     */
    Page<LibraryIssueResponse> getOverdueIssuesByUser(String userUuid, Pageable pageable);

    /**
     * Update issue status (e.g., mark as OVERDUE)
     */
    LibraryIssueResponse updateIssueStatus(String issueUuid, LibraryIssueStatus status);

    /**
     * Calculate fine for an issue
     */
    java.math.BigDecimal calculateFine(String issueUuid);

    /**
     * Get library summary statistics
     */
    LibrarySummary getLibrarySummary();
}

