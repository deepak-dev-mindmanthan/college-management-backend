package org.collegemanagement.repositories;

import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.enums.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {

    /**
     * Find student fee by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.uuid = :uuid
            AND sf.student.college.id = :collegeId
            """)
    Optional<StudentFee> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find student fee by student ID and fee structure ID
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.student.id = :studentId
            AND sf.feeStructure.id = :feeStructureId
            """)
    Optional<StudentFee> findByStudentIdAndFeeStructureId(@Param("studentId") Long studentId, @Param("feeStructureId") Long feeStructureId);

    /**
     * Find student fee by student UUID and fee structure UUID and college ID
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.student.uuid = :studentUuid
            AND sf.feeStructure.uuid = :feeStructureUuid
            AND sf.student.college.id = :collegeId
            """)
    Optional<StudentFee> findByStudentUuidAndFeeStructureUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("feeStructureUuid") String feeStructureUuid,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find all student fees by student UUID and college ID with pagination
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.student.uuid = :studentUuid
            AND sf.student.college.id = :collegeId
            ORDER BY sf.createdAt DESC
            """)
    Page<StudentFee> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all student fees by student UUID and college ID
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.student.uuid = :studentUuid
            AND sf.student.college.id = :collegeId
            ORDER BY sf.createdAt DESC
            """)
    List<StudentFee> findAllByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all student fees by fee structure UUID and college ID with pagination
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.feeStructure.uuid = :feeStructureUuid
            AND sf.feeStructure.college.id = :collegeId
            ORDER BY sf.student.rollNumber ASC
            """)
    Page<StudentFee> findByFeeStructureUuidAndCollegeId(@Param("feeStructureUuid") String feeStructureUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all student fees by status and college ID with pagination
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.status = :status
            AND sf.student.college.id = :collegeId
            ORDER BY sf.dueAmount DESC, sf.createdAt DESC
            """)
    Page<StudentFee> findByStatusAndCollegeId(@Param("status") FeeStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all overdue student fees (status = OVERDUE) by college ID with pagination
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.status = 'OVERDUE'
            AND sf.student.college.id = :collegeId
            ORDER BY sf.dueAmount DESC, sf.createdAt DESC
            """)
    Page<StudentFee> findOverdueFeesByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all student fees by class UUID and college ID with pagination
     */
    @Query("""
            SELECT sf FROM StudentFee sf
            WHERE sf.feeStructure.classRoom.uuid = :classUuid
            AND sf.feeStructure.college.id = :collegeId
            ORDER BY sf.student.rollNumber ASC
            """)
    Page<StudentFee> findByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Count student fees by status and college ID
     */
    @Query("""
            SELECT COUNT(sf) FROM StudentFee sf
            WHERE sf.status = :status
            AND sf.student.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") FeeStatus status, @Param("collegeId") Long collegeId);

    /**
     * Calculate total due amount by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(sf.dueAmount), 0) FROM StudentFee sf
            WHERE sf.student.college.id = :collegeId
            """)
    BigDecimal calculateTotalDueAmountByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Calculate total paid amount by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(sf.paidAmount), 0) FROM StudentFee sf
            WHERE sf.student.college.id = :collegeId
            """)
    BigDecimal calculateTotalPaidAmountByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Calculate total amount by college ID
     */
    @Query("""
            SELECT COALESCE(SUM(sf.totalAmount), 0) FROM StudentFee sf
            WHERE sf.student.college.id = :collegeId
            """)
    BigDecimal calculateTotalAmountByCollegeId(@Param("collegeId") Long collegeId);
}

