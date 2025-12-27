package org.collegemanagement.services;

import org.collegemanagement.dto.librarian.CreateLibrarianRequest;
import org.collegemanagement.dto.librarian.LibrarianResponse;
import org.collegemanagement.dto.librarian.UpdateLibrarianRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibrarianService {

    /**
     * Create a new librarian
     */
    LibrarianResponse createLibrarian(CreateLibrarianRequest request);

    /**
     * Update librarian information
     */
    LibrarianResponse updateLibrarian(String librarianUuid, UpdateLibrarianRequest request);

    /**
     * Get librarian by UUID
     */
    LibrarianResponse getLibrarianByUuid(String librarianUuid);

    /**
     * Get all librarians with pagination
     */
    Page<LibrarianResponse> getAllLibrarians(Pageable pageable);

    /**
     * Search librarians by name or email
     */
    Page<LibrarianResponse> searchLibrarians(String searchTerm, Pageable pageable);

    /**
     * Delete librarian by UUID
     */
    void deleteLibrarian(String librarianUuid);
}

