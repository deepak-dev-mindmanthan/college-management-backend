package org.collegemanagement.repositories;

import org.collegemanagement.entity.library.LibraryIssue;
import org.collegemanagement.enums.LibraryIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LibraryIssueRepository extends JpaRepository<LibraryIssue, Long> {

    /**
     * Find issue by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.uuid = :uuid
            AND b.college.id = :collegeId
            """)
    Optional<LibraryIssue> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all issues by college ID with pagination
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE b.college.id = :collegeId
            ORDER BY i.issueDate DESC, i.dueDate ASC
            """)
    Page<LibraryIssue> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find issues by status and college ID
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.status = :status
            AND b.college.id = :collegeId
            ORDER BY i.dueDate ASC
            """)
    Page<LibraryIssue> findByStatusAndCollegeId(@Param("status") LibraryIssueStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find issues by user (issued to) and college ID
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.issuedTo.id = :userId
            AND b.college.id = :collegeId
            ORDER BY i.issueDate DESC
            """)
    Page<LibraryIssue> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active issues (ISSUED status) by user and college ID
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.issuedTo.id = :userId
            AND i.status = :status
            AND b.college.id = :collegeId
            ORDER BY i.dueDate ASC
            """)
    List<LibraryIssue> findActiveIssuesByUserIdAndCollegeId(@Param("userId") Long userId, @Param("status") LibraryIssueStatus status, @Param("collegeId") Long collegeId);

    /**
     * Find overdue issues (ISSUED status with due date < today) by college ID
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.status = :status
            AND i.dueDate < :today
            AND b.college.id = :collegeId
            ORDER BY i.dueDate ASC
            """)
    Page<LibraryIssue> findOverdueIssuesByCollegeId(@Param("status") LibraryIssueStatus status, @Param("today") LocalDate today, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find overdue issues by user and college ID
     */
    @Query("""
            SELECT i FROM LibraryIssue i
            JOIN i.book b
            WHERE i.issuedTo.id = :userId
            AND i.status = :status
            AND i.dueDate < :today
            AND b.college.id = :collegeId
            ORDER BY i.dueDate ASC
            """)
    List<LibraryIssue> findOverdueIssuesByUserIdAndCollegeId(@Param("userId") Long userId, @Param("status") LibraryIssueStatus status, @Param("today") LocalDate today, @Param("collegeId") Long collegeId);

    /**
     * Count issues by status and college ID
     */
    @Query("""
            SELECT COUNT(i) FROM LibraryIssue i
            JOIN i.book b
            WHERE i.status = :status
            AND b.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") LibraryIssueStatus status, @Param("collegeId") Long collegeId);

    /**
     * Count total issues by college ID
     */
    @Query("""
            SELECT COUNT(i) FROM LibraryIssue i
            JOIN i.book b
            WHERE b.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Calculate total fines by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(i.fineAmount), 0) FROM LibraryIssue i
            JOIN i.book b
            WHERE b.college.id = :collegeId
            AND i.fineAmount IS NOT NULL
            """)
    java.math.BigDecimal sumFinesByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Calculate pending fines (fines for ISSUED or OVERDUE status) by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(i.fineAmount), 0) FROM LibraryIssue i
            JOIN i.book b
            WHERE b.college.id = :collegeId
            AND i.status IN (:statuses)
            AND i.fineAmount IS NOT NULL
            """)
    java.math.BigDecimal sumPendingFinesByCollegeId(@Param("collegeId") Long collegeId, @Param("statuses") List<LibraryIssueStatus> statuses);

    /**
     * Check if user has active issue for a book
     */
    @Query("""
            SELECT COUNT(i) > 0 FROM LibraryIssue i
            WHERE i.book.id = :bookId
            AND i.issuedTo.id = :userId
            AND i.status = :status
            """)
    boolean existsActiveIssueByBookIdAndUserId(@Param("bookId") Long bookId, @Param("userId") Long userId, @Param("status") LibraryIssueStatus status);
}

