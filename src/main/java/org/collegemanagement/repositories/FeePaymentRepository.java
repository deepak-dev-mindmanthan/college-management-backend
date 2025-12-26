package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.FeePayment;
import org.collegemanagement.enums.PaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    /**
     * Find fee payment by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.uuid = :uuid
            AND fp.studentFee.student.college.id = :collegeId
            """)
    Optional<FeePayment> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all fee payments by student fee UUID and college ID with pagination
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.studentFee.uuid = :studentFeeUuid
            AND fp.studentFee.student.college.id = :collegeId
            ORDER BY fp.paymentDate DESC
            """)
    Page<FeePayment> findByStudentFeeUuidAndCollegeId(@Param("studentFeeUuid") String studentFeeUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all fee payments by student fee UUID and college ID
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.studentFee.uuid = :studentFeeUuid
            AND fp.studentFee.student.college.id = :collegeId
            ORDER BY fp.paymentDate DESC
            """)
    List<FeePayment> findAllByStudentFeeUuidAndCollegeId(@Param("studentFeeUuid") String studentFeeUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all fee payments by student UUID and college ID with pagination
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.studentFee.student.uuid = :studentUuid
            AND fp.studentFee.student.college.id = :collegeId
            ORDER BY fp.paymentDate DESC
            """)
    Page<FeePayment> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all fee payments by payment mode and college ID with pagination
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.paymentMode = :paymentMode
            AND fp.studentFee.student.college.id = :collegeId
            ORDER BY fp.paymentDate DESC
            """)
    Page<FeePayment> findByPaymentModeAndCollegeId(@Param("paymentMode") PaymentMode paymentMode, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all fee payments by date range and college ID with pagination
     */
    @Query("""
            SELECT fp FROM FeePayment fp
            WHERE fp.paymentDate >= :startDate
            AND fp.paymentDate <= :endDate
            AND fp.studentFee.student.college.id = :collegeId
            ORDER BY fp.paymentDate DESC
            """)
    Page<FeePayment> findByPaymentDateRangeAndCollegeId(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Calculate total payment amount by student fee ID
     */
    @Query("""
            SELECT COALESCE(SUM(fp.amount), 0) FROM FeePayment fp
            WHERE fp.studentFee.id = :studentFeeId
            """)
    BigDecimal calculateTotalPaymentByStudentFeeId(@Param("studentFeeId") Long studentFeeId);

    /**
     * Calculate total payment amount by college ID and date range
     */
    @Query("""
            SELECT COALESCE(SUM(fp.amount), 0) FROM FeePayment fp
            WHERE fp.studentFee.student.college.id = :collegeId
            AND fp.paymentDate >= :startDate
            AND fp.paymentDate <= :endDate
            """)
    BigDecimal calculateTotalPaymentByCollegeIdAndDateRange(
            @Param("collegeId") Long collegeId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    /**
     * Check if transaction ID exists
     */
    @Query("""
            SELECT COUNT(fp) > 0 FROM FeePayment fp
            WHERE fp.transactionId = :transactionId
            AND fp.studentFee.student.college.id = :collegeId
            """)
    boolean existsByTransactionIdAndCollegeId(@Param("transactionId") String transactionId, @Param("collegeId") Long collegeId);
}

