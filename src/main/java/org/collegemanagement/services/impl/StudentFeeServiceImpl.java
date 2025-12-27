package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.fees.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.fees.FeeComponent;
import org.collegemanagement.entity.fees.FeePayment;
import org.collegemanagement.entity.fees.FeeStructure;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.StudentFeeMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.StudentFeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentFeeServiceImpl implements StudentFeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    // ========== Fee Structure Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public FeeStructureResponse createFeeStructure(CreateFeeStructureRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find and validate class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Check if fee structure already exists for this class
        if (feeStructureRepository.existsByClassIdAndCollegeId(classRoom.getId(), collegeId)) {
            throw new ResourceConflictException(
                    "Fee structure already exists for class " + classRoom.getName() + " " +
                    (classRoom.getSection() != null ? classRoom.getSection() : ""));
        }

        // Calculate total amount from components
        BigDecimal totalAmount = request.getComponents().stream()
                .map(FeeComponentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create fee structure
        FeeStructure feeStructure = FeeStructure.builder()
                .college(college)
                .classRoom(classRoom)
                .totalAmount(totalAmount)
                .components(new HashSet<>())
                .build();

        // Create and add fee components
        Set<FeeComponent> components = new HashSet<>();
        for (FeeComponentRequest componentRequest : request.getComponents()) {
            FeeComponent component = FeeComponent.builder()
                    .feeStructure(feeStructure)
                    .name(componentRequest.getName())
                    .amount(componentRequest.getAmount())
                    .build();
            components.add(component);
        }

        feeStructure.setComponents(components);
        feeStructure = feeStructureRepository.save(feeStructure);

        return StudentFeeMapper.toFeeStructureResponse(feeStructure);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public FeeStructureResponse getFeeStructureByUuid(String feeStructureUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        FeeStructure feeStructure = feeStructureRepository.findByUuidAndCollegeId(feeStructureUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + feeStructureUuid));

        return StudentFeeMapper.toFeeStructureResponse(feeStructure);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public FeeStructureResponse getFeeStructureByClassUuid(String classUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        FeeStructure feeStructure = feeStructureRepository.findByClassUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class with UUID: " + classUuid));

        return StudentFeeMapper.toFeeStructureResponse(feeStructure);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public FeeStructureResponse updateFeeStructure(String feeStructureUuid, UpdateFeeStructureRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find fee structure
        FeeStructure feeStructure = feeStructureRepository.findByUuidAndCollegeId(feeStructureUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + feeStructureUuid));

        // Calculate new total amount
        BigDecimal totalAmount = request.getComponents().stream()
                .map(FeeComponentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Clear existing components and add new ones
        feeStructure.getComponents().clear();
        Set<FeeComponent> components = new HashSet<>();
        for (FeeComponentRequest componentRequest : request.getComponents()) {
            FeeComponent component = FeeComponent.builder()
                    .feeStructure(feeStructure)
                    .name(componentRequest.getName())
                    .amount(componentRequest.getAmount())
                    .build();
            components.add(component);
        }

        feeStructure.setComponents(components);
        feeStructure.setTotalAmount(totalAmount);
        feeStructure = feeStructureRepository.save(feeStructure);

        return StudentFeeMapper.toFeeStructureResponse(feeStructure);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteFeeStructure(String feeStructureUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        FeeStructure feeStructure = feeStructureRepository.findByUuidAndCollegeId(feeStructureUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + feeStructureUuid));

        // Check if any student fees are associated with this structure
        long studentFeeCount = studentFeeRepository.findByFeeStructureUuidAndCollegeId(
                feeStructureUuid, collegeId, Pageable.unpaged()).getTotalElements();

        if (studentFeeCount > 0) {
            throw new ResourceConflictException(
                    "Cannot delete fee structure. " + studentFeeCount + " student(s) have fees assigned to this structure.");
        }

        feeStructureRepository.delete(feeStructure);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<FeeStructureResponse> getAllFeeStructures(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<FeeStructure> feeStructures = feeStructureRepository.findAllByCollegeId(collegeId, pageable);

        return feeStructures.map(StudentFeeMapper::toFeeStructureResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<FeeStructureResponse> getFeeStructuresByClassUuid(String classUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        List<FeeStructure> feeStructures = feeStructureRepository.findAllByClassUuidAndCollegeId(classUuid, collegeId);

        return feeStructures.stream()
                .map(StudentFeeMapper::toFeeStructureResponse)
                .collect(Collectors.toList());
    }

    // ========== Student Fee Assignment ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public StudentFeeResponse assignFeeToStudent(AssignFeeToStudentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Find fee structure
        FeeStructure feeStructure = feeStructureRepository.findByUuidAndCollegeId(request.getFeeStructureUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + request.getFeeStructureUuid()));

        // Check if fee is already assigned
        if (studentFeeRepository.findByStudentIdAndFeeStructureId(student.getId(), feeStructure.getId()).isPresent()) {
            throw new ResourceConflictException(
                    "Fee structure is already assigned to student " + student.getUser().getName());
        }

        // Create student fee
        StudentFee studentFee = StudentFee.builder()
                .student(student)
                .feeStructure(feeStructure)
                .totalAmount(feeStructure.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .dueAmount(feeStructure.getTotalAmount())
                .status(FeeStatus.PENDING)
                .payments(new HashSet<>())
                .build();

        studentFee = studentFeeRepository.save(studentFee);

        return StudentFeeMapper.toStudentFeeResponse(studentFee);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public List<StudentFeeResponse> assignFeeToClassStudents(String classUuid, String feeStructureUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Find fee structure
        FeeStructure feeStructure = feeStructureRepository.findByUuidAndCollegeId(feeStructureUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + feeStructureUuid));

        // Get all active enrollments for the class
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassUuidAndCollegeId(classUuid, collegeId);

        if (enrollments.isEmpty()) {
            throw new ResourceNotFoundException("No active students found in class " + classRoom.getName());
        }

        List<StudentFeeResponse> assignedFees = new ArrayList<>();

        for (StudentEnrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();

            // Check if fee is already assigned
            if (studentFeeRepository.findByStudentIdAndFeeStructureId(student.getId(), feeStructure.getId()).isEmpty()) {
                // Create student fee
                StudentFee studentFee = StudentFee.builder()
                        .student(student)
                        .feeStructure(feeStructure)
                        .totalAmount(feeStructure.getTotalAmount())
                        .paidAmount(BigDecimal.ZERO)
                        .dueAmount(feeStructure.getTotalAmount())
                        .status(FeeStatus.PENDING)
                        .payments(new HashSet<>())
                        .build();

                studentFee = studentFeeRepository.save(studentFee);
                assignedFees.add(StudentFeeMapper.toStudentFeeResponse(studentFee));
            }
        }

        return assignedFees;
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public StudentFeeResponse getStudentFeeByUuid(String studentFeeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));

        return StudentFeeMapper.toStudentFeeResponse(studentFee);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<StudentFeeResponse> getStudentFeesByStudentUuid(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<StudentFee> studentFees = studentFeeRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId, pageable);

        return studentFees.map(StudentFeeMapper::toStudentFeeResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<StudentFeeResponse> getAllStudentFeesByStudentUuid(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<StudentFee> studentFees = studentFeeRepository.findAllByStudentUuidAndCollegeId(studentUuid, collegeId);

        return StudentFeeMapper.toStudentFeeResponseList(studentFees);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<StudentFeeResponse> getStudentFeesByFeeStructureUuid(String feeStructureUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate fee structure exists
        feeStructureRepository.findByUuidAndCollegeId(feeStructureUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with UUID: " + feeStructureUuid));

        Page<StudentFee> studentFees = studentFeeRepository.findByFeeStructureUuidAndCollegeId(feeStructureUuid, collegeId, pageable);

        return studentFees.map(StudentFeeMapper::toStudentFeeResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<StudentFeeResponse> getStudentFeesByStatus(FeeStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<StudentFee> studentFees = studentFeeRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return studentFees.map(StudentFeeMapper::toStudentFeeResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<StudentFeeResponse> getOverdueStudentFees(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<StudentFee> studentFees = studentFeeRepository.findOverdueFeesByCollegeId(collegeId, pageable);

        return studentFees.map(StudentFeeMapper::toStudentFeeResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<StudentFeeResponse> getStudentFeesByClassUuid(String classUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        Page<StudentFee> studentFees = studentFeeRepository.findByClassUuidAndCollegeId(classUuid, collegeId, pageable);

        return studentFees.map(StudentFeeMapper::toStudentFeeResponse);
    }

    // ========== Fee Payment Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public FeePaymentResponse recordFeePayment(CreateFeePaymentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student fee
        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(request.getStudentFeeUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + request.getStudentFeeUuid()));

        // Validate payment amount
        if (request.getAmount().compareTo(studentFee.getDueAmount()) > 0) {
            throw new ResourceConflictException(
                    "Payment amount (" + request.getAmount() + ") cannot exceed due amount (" + studentFee.getDueAmount() + ")");
        }

        // Check transaction ID uniqueness if provided
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
            if (feePaymentRepository.existsByTransactionIdAndCollegeId(request.getTransactionId(), collegeId)) {
                throw new ResourceConflictException("Transaction ID already exists: " + request.getTransactionId());
            }
        }

        // Create fee payment
        FeePayment feePayment = FeePayment.builder()
                .studentFee(studentFee)
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .transactionId(request.getTransactionId())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : Instant.now())
                .build();

        feePayment = feePaymentRepository.save(feePayment);

        // Update student fee
        BigDecimal newPaidAmount = studentFee.getPaidAmount().add(request.getAmount());
        BigDecimal newDueAmount = studentFee.getTotalAmount().subtract(newPaidAmount);

        studentFee.setPaidAmount(newPaidAmount);
        studentFee.setDueAmount(newDueAmount);

        // Update status
        FeeStatus newStatus = StudentFeeMapper.calculateFeeStatus(
                studentFee.getTotalAmount(), newPaidAmount, newDueAmount);
        studentFee.setStatus(newStatus);

        // Add payment to student fee's payment collection
        studentFee.getPayments().add(feePayment);
        studentFeeRepository.save(studentFee);

        return StudentFeeMapper.toFeePaymentResponse(feePayment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public FeePaymentResponse getFeePaymentByUuid(String paymentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        FeePayment feePayment = feePaymentRepository.findByUuidAndCollegeId(paymentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee payment not found with UUID: " + paymentUuid));

        return StudentFeeMapper.toFeePaymentResponse(feePayment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<FeePaymentResponse> getFeePaymentsByStudentFeeUuid(String studentFeeUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student fee exists
        studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));

        Page<FeePayment> feePayments = feePaymentRepository.findByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId, pageable);

        return feePayments.map(StudentFeeMapper::toFeePaymentResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<FeePaymentResponse> getAllFeePaymentsByStudentFeeUuid(String studentFeeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student fee exists
        studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));

        List<FeePayment> feePayments = feePaymentRepository.findAllByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId);

        return StudentFeeMapper.toFeePaymentResponseList(feePayments);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<FeePaymentResponse> getFeePaymentsByStudentUuid(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<FeePayment> feePayments = feePaymentRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId, pageable);

        return feePayments.map(StudentFeeMapper::toFeePaymentResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public Page<FeePaymentResponse> getFeePaymentsByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        Page<FeePayment> feePayments = feePaymentRepository.findByPaymentDateRangeAndCollegeId(startDate, endDate, collegeId, pageable);

        return feePayments.map(StudentFeeMapper::toFeePaymentResponse);
    }

    // ========== Summary and Reports ==========

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public StudentFeeSummaryResponse getStudentFeeSummary(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Get all student fees
        List<StudentFee> studentFees = studentFeeRepository.findAllByStudentUuidAndCollegeId(studentUuid, collegeId);

        // Get active enrollment for class info
        StudentEnrollment enrollment = studentEnrollmentRepository.findActiveByStudentIdAndCollegeId(student.getId(), collegeId)
                .orElse(null);

        // Calculate summary
        BigDecimal totalFees = studentFees.stream()
                .map(StudentFee::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = studentFees.stream()
                .map(StudentFee::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDue = studentFees.stream()
                .map(StudentFee::getDueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PENDING)
                .count();

        long paidCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PAID)
                .count();

        long partiallyPaidCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PARTIALLY_PAID)
                .count();

        long overdueCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.OVERDUE)
                .count();

        return StudentFeeSummaryResponse.builder()
                .studentUuid(student.getUuid())
                .studentName(student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student.getRollNumber())
                .className(enrollment != null && enrollment.getClassRoom() != null ?
                        enrollment.getClassRoom().getName() : null)
                .totalFees(totalFees)
                .totalPaid(totalPaid)
                .totalDue(totalDue)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .partiallyPaidCount(partiallyPaidCount)
                .overdueCount(overdueCount)
                .fees(StudentFeeMapper.toStudentFeeResponseList(studentFees))
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public CollegeFeeSummaryResponse getCollegeFeeSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        BigDecimal totalFees = studentFeeRepository.calculateTotalAmountByCollegeId(collegeId);
        BigDecimal totalPaid = studentFeeRepository.calculateTotalPaidAmountByCollegeId(collegeId);
        BigDecimal totalDue = studentFeeRepository.calculateTotalDueAmountByCollegeId(collegeId);

        long totalStudents = studentRepository.countByCollegeId(collegeId);
        long pendingCount = studentFeeRepository.countByStatusAndCollegeId(FeeStatus.PENDING, collegeId);
        long paidCount = studentFeeRepository.countByStatusAndCollegeId(FeeStatus.PAID, collegeId);
        long partiallyPaidCount = studentFeeRepository.countByStatusAndCollegeId(FeeStatus.PARTIALLY_PAID, collegeId);
        long overdueCount = studentFeeRepository.countByStatusAndCollegeId(FeeStatus.OVERDUE, collegeId);

        return CollegeFeeSummaryResponse.builder()
                .totalFees(totalFees != null ? totalFees : BigDecimal.ZERO)
                .totalPaid(totalPaid != null ? totalPaid : BigDecimal.ZERO)
                .totalDue(totalDue != null ? totalDue : BigDecimal.ZERO)
                .totalStudents(totalStudents)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .partiallyPaidCount(partiallyPaidCount)
                .overdueCount(overdueCount)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER')")
    public ClassFeeSummaryResponse getClassFeeSummary(String classUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Get all student fees for the class
        List<StudentFee> studentFees = studentFeeRepository.findByClassUuidAndCollegeId(
                classUuid, collegeId, Pageable.unpaged()).getContent();

        // Calculate summary
        BigDecimal totalFees = studentFees.stream()
                .map(StudentFee::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = studentFees.stream()
                .map(StudentFee::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDue = studentFees.stream()
                .map(StudentFee::getDueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalStudents = studentFees.stream()
                .map(StudentFee::getStudent)
                .distinct()
                .count();

        long pendingCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PENDING)
                .count();

        long paidCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PAID)
                .count();

        long partiallyPaidCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.PARTIALLY_PAID)
                .count();

        long overdueCount = studentFees.stream()
                .filter(sf -> sf.getStatus() == FeeStatus.OVERDUE)
                .count();

        return ClassFeeSummaryResponse.builder()
                .classUuid(classRoom.getUuid())
                .className(classRoom.getName())
                .section(classRoom.getSection())
                .totalFees(totalFees)
                .totalPaid(totalPaid)
                .totalDue(totalDue)
                .totalStudents(totalStudents)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .partiallyPaidCount(partiallyPaidCount)
                .overdueCount(overdueCount)
                .build();
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

