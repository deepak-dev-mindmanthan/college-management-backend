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
import org.collegemanagement.dto.fees.*;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.services.StudentFeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fees")
@AllArgsConstructor
@Tag(name = "Student Fees Management", description = "APIs for managing student fees, fee structures, and payments in the college management system")
public class StudentFeeController {

    private final StudentFeeService studentFeeService;

    // ========== Fee Structure Management Endpoints ==========

    @Operation(
            summary = "Create fee structure",
            description = "Creates a new fee structure for a class with fee components. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Fee structure created successfully",
                    content = @Content(schema = @Schema(implementation = FeeStructureResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Fee structure already exists for this class"
            )
    })
    @PostMapping("/structures")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> createFeeStructure(
            @Valid @RequestBody CreateFeeStructureRequest request
    ) {
        FeeStructureResponse feeStructure = studentFeeService.createFeeStructure(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(feeStructure, "Fee structure created successfully",HttpStatus.CONTINUE.value()));
    }

    @Operation(
            summary = "Get fee structure by UUID",
            description = "Retrieves fee structure information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/structures/{feeStructureUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> getFeeStructure(
            @Parameter(description = "UUID of the fee structure")
            @PathVariable String feeStructureUuid
    ) {
        FeeStructureResponse feeStructure = studentFeeService.getFeeStructureByUuid(feeStructureUuid);
        return ResponseEntity.ok(ApiResponse.success(feeStructure, "Fee structure retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee structure by class UUID",
            description = "Retrieves fee structure for a specific class. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{classUuid}/structure")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> getFeeStructureByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid
    ) {
        FeeStructureResponse feeStructure = studentFeeService.getFeeStructureByClassUuid(classUuid);
        return ResponseEntity.ok(ApiResponse.success(feeStructure, "Fee structure retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Update fee structure",
            description = "Updates an existing fee structure. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @PutMapping("/structures/{feeStructureUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> updateFeeStructure(
            @Parameter(description = "UUID of the fee structure")
            @PathVariable String feeStructureUuid,
            @Valid @RequestBody UpdateFeeStructureRequest request
    ) {
        FeeStructureResponse feeStructure = studentFeeService.updateFeeStructure(feeStructureUuid, request);
        return ResponseEntity.ok(ApiResponse.success(feeStructure, "Fee structure updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete fee structure",
            description = "Deletes a fee structure. Can only be deleted if no student fees are assigned. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/structures/{feeStructureUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStructure(
            @Parameter(description = "UUID of the fee structure to delete")
            @PathVariable String feeStructureUuid
    ) {
        studentFeeService.deleteFeeStructure(feeStructureUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee structure deleted successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all fee structures",
            description = "Retrieves a paginated list of all fee structures. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/structures")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<FeeStructureResponse>>> getAllFeeStructures(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<FeeStructureResponse> feeStructures = studentFeeService.getAllFeeStructures(pageable);
        return ResponseEntity.ok(ApiResponse.success(feeStructures, "Fee structures retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee structures by class UUID",
            description = "Retrieves all fee structures for a specific class. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{classUuid}/structures")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<FeeStructureResponse>>> getFeeStructuresByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid
    ) {
        List<FeeStructureResponse> feeStructures = studentFeeService.getFeeStructuresByClassUuid(classUuid);
        return ResponseEntity.ok(ApiResponse.success(feeStructures, "Fee structures retrieved successfully",HttpStatus.OK.value()));
    }

    // ========== Student Fee Assignment Endpoints ==========

    @Operation(
            summary = "Assign fee to student",
            description = "Assigns a fee structure to a specific student. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<StudentFeeResponse>> assignFeeToStudent(
            @Valid @RequestBody AssignFeeToStudentRequest request
    ) {
        StudentFeeResponse studentFee = studentFeeService.assignFeeToStudent(request);
        return ResponseEntity.ok(ApiResponse.success(studentFee, "Fee assigned to student successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Assign fee to all students in a class",
            description = "Assigns a fee structure to all active students in a class. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @PostMapping("/classes/{classUuid}/assign/{feeStructureUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<StudentFeeResponse>>> assignFeeToClassStudents(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "UUID of the fee structure")
            @PathVariable String feeStructureUuid
    ) {
        List<StudentFeeResponse> studentFees = studentFeeService.assignFeeToClassStudents(classUuid, feeStructureUuid);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Fee assigned to class students successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student fee by UUID",
            description = "Retrieves student fee information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/student-fees/{studentFeeUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentFeeResponse>> getStudentFee(
            @Parameter(description = "UUID of the student fee")
            @PathVariable String studentFeeUuid
    ) {
        StudentFeeResponse studentFee = studentFeeService.getStudentFeeByUuid(studentFeeUuid);
        return ResponseEntity.ok(ApiResponse.success(studentFee, "Student fee retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all student fees for a student",
            description = "Retrieves a paginated list of all student fees for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/fees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<StudentFeeResponse>>> getStudentFeesByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentFeeResponse> studentFees = studentFeeService.getStudentFeesByStudentUuid(studentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Student fees retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all student fees by fee structure",
            description = "Retrieves a paginated list of all student fees for a specific fee structure. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/structures/{feeStructureUuid}/student-fees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentFeeResponse>>> getStudentFeesByFeeStructure(
            @Parameter(description = "UUID of the fee structure")
            @PathVariable String feeStructureUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentFeeResponse> studentFees = studentFeeService.getStudentFeesByFeeStructureUuid(feeStructureUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Student fees retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student fees by status",
            description = "Retrieves a paginated list of student fees filtered by status. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/student-fees/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentFeeResponse>>> getStudentFeesByStatus(
            @Parameter(description = "Fee status (PENDING, PAID, PARTIALLY_PAID, OVERDUE)")
            @PathVariable FeeStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentFeeResponse> studentFees = studentFeeService.getStudentFeesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Student fees retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get overdue student fees",
            description = "Retrieves a paginated list of all overdue student fees. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/student-fees/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentFeeResponse>>> getOverdueStudentFees(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentFeeResponse> studentFees = studentFeeService.getOverdueStudentFees(pageable);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Overdue student fees retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student fees by class",
            description = "Retrieves a paginated list of all student fees for a specific class. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/classes/{classUuid}/student-fees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentFeeResponse>>> getStudentFeesByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentFeeResponse> studentFees = studentFeeService.getStudentFeesByClassUuid(classUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(studentFees, "Student fees retrieved successfully",HttpStatus.OK.value()));
    }

    // ========== Fee Payment Management Endpoints ==========

    @Operation(
            summary = "Record fee payment",
            description = "Records a payment for a student fee. Updates the fee status automatically. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FeePaymentResponse>> recordFeePayment(
            @Valid @RequestBody CreateFeePaymentRequest request
    ) {
        FeePaymentResponse feePayment = studentFeeService.recordFeePayment(request);
        return ResponseEntity.ok(ApiResponse.success(feePayment, "Fee payment recorded successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee payment by UUID",
            description = "Retrieves fee payment information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/payments/{paymentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeePaymentResponse>> getFeePayment(
            @Parameter(description = "UUID of the fee payment")
            @PathVariable String paymentUuid
    ) {
        FeePaymentResponse feePayment = studentFeeService.getFeePaymentByUuid(paymentUuid);
        return ResponseEntity.ok(ApiResponse.success(feePayment, "Fee payment retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee payments for a student fee",
            description = "Retrieves a paginated list of all payments for a specific student fee. Accessible by all authenticated users."
    )
    @GetMapping("/student-fees/{studentFeeUuid}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<FeePaymentResponse>>> getFeePaymentsByStudentFee(
            @Parameter(description = "UUID of the student fee")
            @PathVariable String studentFeeUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FeePaymentResponse> feePayments = studentFeeService.getFeePaymentsByStudentFeeUuid(studentFeeUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(feePayments, "Fee payments retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee payments for a student",
            description = "Retrieves a paginated list of all payments for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<FeePaymentResponse>>> getFeePaymentsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FeePaymentResponse> feePayments = studentFeeService.getFeePaymentsByStudentUuid(studentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(feePayments, "Fee payments retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get fee payments by date range",
            description = "Retrieves a paginated list of all payments within a date range. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/payments/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<FeePaymentResponse>>> getFeePaymentsByDateRange(
            @Parameter(description = "Start date (ISO 8601 format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date (ISO 8601 format)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FeePaymentResponse> feePayments = studentFeeService.getFeePaymentsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(feePayments, "Fee payments retrieved successfully",HttpStatus.OK.value()));
    }

    // ========== Summary and Reports Endpoints ==========

    @Operation(
            summary = "Get student fee summary",
            description = "Retrieves a comprehensive fee summary for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentFeeSummaryResponse>> getStudentFeeSummary(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        StudentFeeSummaryResponse summary = studentFeeService.getStudentFeeSummary(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(summary, "Student fee summary retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get college fee summary",
            description = "Retrieves a comprehensive fee summary for the entire college. Requires COLLEGE_ADMIN, SUPER_ADMIN, or ACCOUNTANT role."
    )
    @GetMapping("/summary/college")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<CollegeFeeSummaryResponse>> getCollegeFeeSummary() {
        CollegeFeeSummaryResponse summary = studentFeeService.getCollegeFeeSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "College fee summary retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get class fee summary",
            description = "Retrieves a comprehensive fee summary for a specific class. Requires COLLEGE_ADMIN, SUPER_ADMIN, ACCOUNTANT, or TEACHER role."
    )
    @GetMapping("/classes/{classUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClassFeeSummaryResponse>> getClassFeeSummary(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid
    ) {
        ClassFeeSummaryResponse summary = studentFeeService.getClassFeeSummary(classUuid);
        return ResponseEntity.ok(ApiResponse.success(summary, "Class fee summary retrieved successfully",HttpStatus.OK.value()));
    }
}

