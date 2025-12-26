package org.collegemanagement.repositories;

import org.collegemanagement.entity.library.LibraryBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {

    /**
     * Find book by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT b FROM LibraryBook b
            WHERE b.uuid = :uuid
            AND b.college.id = :collegeId
            """)
    Optional<LibraryBook> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all books by college ID with pagination
     */
    @Query("""
            SELECT b FROM LibraryBook b
            WHERE b.college.id = :collegeId
            ORDER BY b.title ASC, b.author ASC
            """)
    Page<LibraryBook> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Search books by title, author, ISBN, or category within a college
     */
    @Query("""
            SELECT b FROM LibraryBook b
            WHERE b.college.id = :collegeId
            AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(b.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY b.title ASC, b.author ASC
            """)
    Page<LibraryBook> searchBooksByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find books by category and college ID
     */
    @Query("""
            SELECT b FROM LibraryBook b
            WHERE b.college.id = :collegeId
            AND LOWER(b.category) = LOWER(:category)
            ORDER BY b.title ASC, b.author ASC
            """)
    Page<LibraryBook> findByCategoryAndCollegeId(@Param("collegeId") Long collegeId, @Param("category") String category, Pageable pageable);

    /**
     * Find available books (availableCopies > 0) by college ID
     */
    @Query("""
            SELECT b FROM LibraryBook b
            WHERE b.college.id = :collegeId
            AND b.availableCopies > 0
            ORDER BY b.title ASC, b.author ASC
            """)
    Page<LibraryBook> findAvailableBooksByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Check if ISBN exists within a college
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM LibraryBook b
            WHERE b.isbn = :isbn
            AND b.college.id = :collegeId
            AND b.isbn IS NOT NULL
            """)
    boolean existsByIsbnAndCollegeId(@Param("isbn") String isbn, @Param("collegeId") Long collegeId);

    /**
     * Check if ISBN exists within a college (excluding a specific book for updates)
     */
    @Query("""
            SELECT COUNT(b) > 0 FROM LibraryBook b
            WHERE b.isbn = :isbn
            AND b.college.id = :collegeId
            AND b.id != :excludeId
            AND b.isbn IS NOT NULL
            """)
    boolean existsByIsbnAndCollegeIdAndIdNot(@Param("isbn") String isbn, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Count total books by college ID
     */
    @Query("""
            SELECT COUNT(b) FROM LibraryBook b
            WHERE b.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Count total copies by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(b.totalCopies), 0) FROM LibraryBook b
            WHERE b.college.id = :collegeId
            """)
    long countTotalCopiesByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Count available copies by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(b.availableCopies), 0) FROM LibraryBook b
            WHERE b.college.id = :collegeId
            """)
    long countAvailableCopiesByCollegeId(@Param("collegeId") Long collegeId);
}

