package org.collegemanagement.repositories;

import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoice by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.uuid = :uuid
            AND i.college.id = :collegeId
            """)
    Optional<Invoice> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find invoice by invoice number and college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.invoiceNumber = :invoiceNumber
            AND i.college.id = :collegeId
            """)
    Optional<Invoice> findByInvoiceNumberAndCollegeId(@Param("invoiceNumber") String invoiceNumber, @Param("collegeId") Long collegeId);

    /**
     * Find all invoices by college ID with pagination
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.college.id = :collegeId
            ORDER BY i.createdAt DESC
            """)
    Page<Invoice> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find invoices by subscription and college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.subscription.id = :subscriptionId
            AND i.college.id = :collegeId
            ORDER BY i.createdAt DESC
            """)
    List<Invoice> findBySubscriptionIdAndCollegeId(@Param("subscriptionId") Long subscriptionId, @Param("collegeId") Long collegeId);

    /**
     * Find invoices by status and college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.status = :status
            AND i.college.id = :collegeId
            ORDER BY i.createdAt DESC
            """)
    Page<Invoice> findByStatusAndCollegeId(@Param("status") InvoiceStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find invoices by due date range and college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.college.id = :collegeId
            AND i.dueDate BETWEEN :startDate AND :endDate
            ORDER BY i.dueDate ASC
            """)
    Page<Invoice> findByDueDateRangeAndCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Find overdue invoices by college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.college.id = :collegeId
            AND i.dueDate < :currentDate
            AND i.status = 'UNPAID'
            ORDER BY i.dueDate ASC
            """)
    List<Invoice> findOverdueInvoicesByCollegeId(@Param("collegeId") Long collegeId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find unpaid invoices by college ID
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.college.id = :collegeId
            AND i.status = 'UNPAID'
            ORDER BY i.dueDate ASC
            """)
    List<Invoice> findUnpaidInvoicesByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Count invoices by status and college ID
     */
    @Query("""
            SELECT COUNT(i) FROM Invoice i
            WHERE i.status = :status
            AND i.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") InvoiceStatus status, @Param("collegeId") Long collegeId);

    /**
     * Check if invoice exists by invoice number
     */
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * Get latest invoice for a subscription
     */
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.subscription.id = :subscriptionId
            AND i.college.id = :collegeId
            ORDER BY i.createdAt DESC
            """)
    Optional<Invoice> findLatestBySubscriptionIdAndCollegeId(@Param("subscriptionId") Long subscriptionId, @Param("collegeId") Long collegeId);
}

