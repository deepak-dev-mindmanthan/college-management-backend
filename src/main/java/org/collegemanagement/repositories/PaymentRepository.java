package org.collegemanagement.repositories;

import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.enums.PaymentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE p.uuid = :uuid
            AND i.college.id = :collegeId
            """)
    Optional<Payment> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find payment by transaction ID and college ID
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE p.transactionId = :transactionId
            AND i.college.id = :collegeId
            """)
    Optional<Payment> findByTransactionIdAndCollegeId(@Param("transactionId") String transactionId, @Param("collegeId") Long collegeId);

    /**
     * Find all payments by college ID with pagination
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE i.college.id = :collegeId
            ORDER BY p.paymentDate DESC
            """)
    Page<Payment> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find payments by invoice and college ID
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE p.invoice.id = :invoiceId
            AND i.college.id = :collegeId
            ORDER BY p.paymentDate DESC
            """)
    List<Payment> findByInvoiceIdAndCollegeId(@Param("invoiceId") Long invoiceId, @Param("collegeId") Long collegeId);

    /**
     * Find payments by status and college ID
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE p.status = :status
            AND i.college.id = :collegeId
            ORDER BY p.paymentDate DESC
            """)
    Page<Payment> findByStatusAndCollegeId(@Param("status") PaymentStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find payments by gateway and college ID
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE p.gateway = :gateway
            AND i.college.id = :collegeId
            ORDER BY p.paymentDate DESC
            """)
    Page<Payment> findByGatewayAndCollegeId(@Param("gateway") PaymentGateway gateway, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find payments by date range and college ID
     */
    @Query("""
            SELECT p FROM Payment p
            JOIN p.invoice i
            WHERE i.college.id = :collegeId
            AND p.paymentDate BETWEEN :startDate AND :endDate
            ORDER BY p.paymentDate DESC
            """)
    Page<Payment> findByDateRangeAndCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Count payments by status and college ID
     */
    @Query("""
            SELECT COUNT(p) FROM Payment p
            JOIN p.invoice i
            WHERE p.status = :status
            AND i.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") PaymentStatus status, @Param("collegeId") Long collegeId);

    /**
     * Check if payment exists by transaction ID
     */
    boolean existsByTransactionId(String transactionId);
}

