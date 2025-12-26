package org.collegemanagement.services;

import org.collegemanagement.dto.fees.*;
import org.collegemanagement.enums.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface StudentFeeService {

    // ========== Fee Structure Management ==========

    /**
     * Create a new fee structure for a class
     */
    FeeStructureResponse createFeeStructure(CreateFeeStructureRequest request);

    /**
     * Get fee structure by UUID
     */
    FeeStructureResponse getFeeStructureByUuid(String feeStructureUuid);

    /**
     * Get fee structure by class UUID
     */
    FeeStructureResponse getFeeStructureByClassUuid(String classUuid);

    /**
     * Update fee structure
     */
    FeeStructureResponse updateFeeStructure(String feeStructureUuid, UpdateFeeStructureRequest request);

    /**
     * Delete fee structure
     */
    void deleteFeeStructure(String feeStructureUuid);

    /**
     * Get all fee structures with pagination
     */
    Page<FeeStructureResponse> getAllFeeStructures(Pageable pageable);

    /**
     * Get all fee structures by class UUID
     */
    List<FeeStructureResponse> getFeeStructuresByClassUuid(String classUuid);

    // ========== Student Fee Assignment ==========

    /**
     * Assign fee structure to a student
     */
    StudentFeeResponse assignFeeToStudent(AssignFeeToStudentRequest request);

    /**
     * Assign fee structure to multiple students in a class
     */
    List<StudentFeeResponse> assignFeeToClassStudents(String classUuid, String feeStructureUuid);

    /**
     * Get student fee by UUID
     */
    StudentFeeResponse getStudentFeeByUuid(String studentFeeUuid);

    /**
     * Get all student fees for a student with pagination
     */
    Page<StudentFeeResponse> getStudentFeesByStudentUuid(String studentUuid, Pageable pageable);

    /**
     * Get all student fees for a student
     */
    List<StudentFeeResponse> getAllStudentFeesByStudentUuid(String studentUuid);

    /**
     * Get all student fees by fee structure UUID with pagination
     */
    Page<StudentFeeResponse> getStudentFeesByFeeStructureUuid(String feeStructureUuid, Pageable pageable);

    /**
     * Get all student fees by status with pagination
     */
    Page<StudentFeeResponse> getStudentFeesByStatus(FeeStatus status, Pageable pageable);

    /**
     * Get all overdue student fees with pagination
     */
    Page<StudentFeeResponse> getOverdueStudentFees(Pageable pageable);

    /**
     * Get all student fees by class UUID with pagination
     */
    Page<StudentFeeResponse> getStudentFeesByClassUuid(String classUuid, Pageable pageable);

    // ========== Fee Payment Management ==========

    /**
     * Record a fee payment
     */
    FeePaymentResponse recordFeePayment(CreateFeePaymentRequest request);

    /**
     * Get fee payment by UUID
     */
    FeePaymentResponse getFeePaymentByUuid(String paymentUuid);

    /**
     * Get all fee payments for a student fee with pagination
     */
    Page<FeePaymentResponse> getFeePaymentsByStudentFeeUuid(String studentFeeUuid, Pageable pageable);

    /**
     * Get all fee payments for a student fee
     */
    List<FeePaymentResponse> getAllFeePaymentsByStudentFeeUuid(String studentFeeUuid);

    /**
     * Get all fee payments for a student with pagination
     */
    Page<FeePaymentResponse> getFeePaymentsByStudentUuid(String studentUuid, Pageable pageable);

    /**
     * Get all fee payments by date range with pagination
     */
    Page<FeePaymentResponse> getFeePaymentsByDateRange(Instant startDate, Instant endDate, Pageable pageable);

    // ========== Summary and Reports ==========

    /**
     * Get student fee summary
     */
    StudentFeeSummaryResponse getStudentFeeSummary(String studentUuid);

    /**
     * Get college fee summary
     */
    CollegeFeeSummaryResponse getCollegeFeeSummary();

    /**
     * Get class fee summary
     */
    ClassFeeSummaryResponse getClassFeeSummary(String classUuid);
}

