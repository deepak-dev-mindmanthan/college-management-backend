package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.fees.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.fees.FeeAdjustment;
import org.collegemanagement.entity.fees.FeeComponent;
import org.collegemanagement.entity.fees.FeeInstallment;
import org.collegemanagement.entity.fees.FeeInstallmentTemplate;
import org.collegemanagement.entity.fees.FeePayment;
import org.collegemanagement.entity.fees.FeeReceipt;
import org.collegemanagement.entity.fees.FeeStructure;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.AdjustmentType;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.enums.InstallmentStatus;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.StudentFeeMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.EmailService;
import org.collegemanagement.services.StudentFeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private final FeeInstallmentRepository feeInstallmentRepository;
    private final FeeAdjustmentRepository feeAdjustmentRepository;
    private final FeeReceiptRepository feeReceiptRepository;
    private final ClassRoomRepository classRoomRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final EmailService emailService;

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

        // Validate installments total if provided
        if (request.getInstallments() != null && !request.getInstallments().isEmpty()) {
            BigDecimal installmentTotal = request.getInstallments().stream()
                    .map(FeeInstallmentTemplateRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (installmentTotal.compareTo(totalAmount) != 0) {
                throw new ResourceConflictException(
                        "Installment total (" + installmentTotal + ") must equal fee total (" + totalAmount + ")");
            }
        }

        // Create fee structure
        FeeStructure feeStructure = FeeStructure.builder()
                .college(college)
                .classRoom(classRoom)
                .totalAmount(totalAmount)
                .dueDate(request.getDueDate())
                .components(new HashSet<>())
                .installmentTemplates(new HashSet<>())
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
        // Create installment templates if provided
        if (request.getInstallments() != null && !request.getInstallments().isEmpty()) {
            Set<FeeInstallmentTemplate> templates = new HashSet<>();
            for (FeeInstallmentTemplateRequest installmentRequest : request.getInstallments()) {
                FeeInstallmentTemplate template = FeeInstallmentTemplate.builder()
                        .feeStructure(feeStructure)
                        .name(installmentRequest.getName())
                        .amount(installmentRequest.getAmount())
                        .dueDate(installmentRequest.getDueDate())
                        .build();
                templates.add(template);
            }
            feeStructure.setInstallmentTemplates(templates);
        }
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

        // Validate installments total if provided
        if (request.getInstallments() != null && !request.getInstallments().isEmpty()) {
            BigDecimal installmentTotal = request.getInstallments().stream()
                    .map(FeeInstallmentTemplateRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (installmentTotal.compareTo(totalAmount) != 0) {
                throw new ResourceConflictException(
                        "Installment total (" + installmentTotal + ") must equal fee total (" + totalAmount + ")");
            }
        }

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
        feeStructure.setDueDate(request.getDueDate());

        // Replace installment templates if provided
        if (request.getInstallments() != null) {
            feeStructure.getInstallmentTemplates().clear();
            Set<FeeInstallmentTemplate> templates = new HashSet<>();
            for (FeeInstallmentTemplateRequest installmentRequest : request.getInstallments()) {
                FeeInstallmentTemplate template = FeeInstallmentTemplate.builder()
                        .feeStructure(feeStructure)
                        .name(installmentRequest.getName())
                        .amount(installmentRequest.getAmount())
                        .dueDate(installmentRequest.getDueDate())
                        .build();
                templates.add(template);
            }
            feeStructure.setInstallmentTemplates(templates);
        }
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
                .netAmount(feeStructure.getTotalAmount())
                .discountAmount(BigDecimal.ZERO)
                .waiverAmount(BigDecimal.ZERO)
                .penaltyAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .dueAmount(feeStructure.getTotalAmount())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : feeStructure.getDueDate())
                .status(FeeStatus.PENDING)
                .payments(new HashSet<>())
                .installments(new HashSet<>())
                .adjustments(new HashSet<>())
                .build();

        studentFee = studentFeeRepository.save(studentFee);

        createInstallmentsForStudentFee(studentFee, feeStructure);

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
                        .netAmount(feeStructure.getTotalAmount())
                        .discountAmount(BigDecimal.ZERO)
                        .waiverAmount(BigDecimal.ZERO)
                        .penaltyAmount(BigDecimal.ZERO)
                        .paidAmount(BigDecimal.ZERO)
                        .dueAmount(feeStructure.getTotalAmount())
                        .dueDate(feeStructure.getDueDate())
                        .status(FeeStatus.PENDING)
                        .payments(new HashSet<>())
                        .installments(new HashSet<>())
                        .adjustments(new HashSet<>())
                        .build();

                studentFee = studentFeeRepository.save(studentFee);
                createInstallmentsForStudentFee(studentFee, feeStructure);
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
        assertStudentFeeAccess(studentFee, collegeId);

        return StudentFeeMapper.toStudentFeeResponse(studentFee);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<StudentFeeResponse> getStudentFeesByStudentUuid(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        assertStudentAccess(studentUuid, collegeId);

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
        assertStudentAccess(studentUuid, collegeId);

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

        // Allocate payment to installments if any
        allocatePaymentToInstallments(studentFee, request.getAmount());

        // Update student fee
        BigDecimal effectiveNet = getEffectiveNetAmount(studentFee);
        if (studentFee.getNetAmount() == null) {
            studentFee.setNetAmount(effectiveNet);
        }
        BigDecimal newPaidAmount = studentFee.getPaidAmount().add(request.getAmount());
        BigDecimal newDueAmount = effectiveNet.subtract(newPaidAmount);

        studentFee.setPaidAmount(newPaidAmount);
        studentFee.setDueAmount(newDueAmount);

        // Update status
        FeeStatus newStatus = StudentFeeMapper.calculateFeeStatus(
                effectiveNet, newPaidAmount, newDueAmount, studentFee.getDueDate());
        studentFee.setStatus(newStatus);

        // Add payment to student fee's payment collection
        studentFee.getPayments().add(feePayment);
        studentFeeRepository.save(studentFee);

        // Create receipt
        FeeReceipt receipt = FeeReceipt.builder()
                .feePayment(feePayment)
                .receiptNumber(generateReceiptNumber())
                .issuedAt(Instant.now())
                .issuedBy(getCurrentUser())
                .build();
        feeReceiptRepository.save(receipt);
        feePayment.setReceipt(receipt);

        // Send payment receipt email (non-blocking)
        try {
            Student student = studentFee.getStudent();
            if (student != null && student.getUser() != null && student.getUser().getEmail() != null) {
                String email = student.getUser().getEmail();
                if (!email.isBlank()) {
                    String collegeName = student.getCollege() != null ? student.getCollege().getName() : "College";
                    String studentName = student.getUser().getName() != null ? student.getUser().getName() : "Student";
                    emailService.sendStudentFeePaymentEmail(
                            email,
                            collegeName,
                            studentName,
                            request.getAmount(),
                            receipt.getReceiptNumber(),
                            request.getTransactionId(),
                            feePayment.getPaymentDate(),
                            studentFee.getDueAmount()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send fee payment email: {}", e.getMessage());
        }

        return StudentFeeMapper.toFeePaymentResponse(feePayment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public FeePaymentResponse getFeePaymentByUuid(String paymentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        FeePayment feePayment = feePaymentRepository.findByUuidAndCollegeId(paymentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee payment not found with UUID: " + paymentUuid));
        if (feePayment.getStudentFee() != null) {
            assertStudentFeeAccess(feePayment.getStudentFee(), collegeId);
        }

        return StudentFeeMapper.toFeePaymentResponse(feePayment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<FeePaymentResponse> getFeePaymentsByStudentFeeUuid(String studentFeeUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student fee exists
        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));
        assertStudentFeeAccess(studentFee, collegeId);

        Page<FeePayment> feePayments = feePaymentRepository.findByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId, pageable);

        return feePayments.map(StudentFeeMapper::toFeePaymentResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<FeePaymentResponse> getAllFeePaymentsByStudentFeeUuid(String studentFeeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student fee exists
        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));
        assertStudentFeeAccess(studentFee, collegeId);

        List<FeePayment> feePayments = feePaymentRepository.findAllByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId);

        return StudentFeeMapper.toFeePaymentResponseList(feePayments);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public Page<FeePaymentResponse> getFeePaymentsByStudentUuid(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        assertStudentAccess(studentUuid, collegeId);

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

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<FeeInstallmentResponse> getFeeInstallmentsByStudentFeeUuid(String studentFeeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));
        assertStudentFeeAccess(studentFee, collegeId);

        List<FeeInstallment> installments = feeInstallmentRepository
                .findByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId);

        return StudentFeeMapper.toFeeInstallmentResponseList(installments);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public FeeAdjustmentResponse applyFeeAdjustment(String studentFeeUuid, FeeAdjustmentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));

        FeeAdjustment adjustment = FeeAdjustment.builder()
                .studentFee(studentFee)
                .type(request.getType())
                .amount(request.getAmount())
                .reason(request.getReason())
                .build();
        adjustment = feeAdjustmentRepository.save(adjustment);
        studentFee.getAdjustments().add(adjustment);

        applyAdjustmentToStudentFee(studentFee, request.getType(), request.getAmount());

        // Send adjustment email (non-blocking)
        try {
            Student student = studentFee.getStudent();
            if (student != null && student.getUser() != null && student.getUser().getEmail() != null) {
                String email = student.getUser().getEmail();
                if (!email.isBlank()) {
                    String collegeName = student.getCollege() != null ? student.getCollege().getName() : "College";
                    String studentName = student.getUser().getName() != null ? student.getUser().getName() : "Student";
                    emailService.sendStudentFeeAdjustmentEmail(
                            email,
                            collegeName,
                            studentName,
                            request.getType(),
                            request.getAmount(),
                            studentFee.getNetAmount(),
                            studentFee.getDueAmount()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send fee adjustment email: {}", e.getMessage());
        }

        return StudentFeeMapper.toFeeAdjustmentResponse(adjustment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public List<FeeAdjustmentResponse> getFeeAdjustmentsByStudentFeeUuid(String studentFeeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        StudentFee studentFee = studentFeeRepository.findByUuidAndCollegeId(studentFeeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student fee not found with UUID: " + studentFeeUuid));
        assertStudentFeeAccess(studentFee, collegeId);

        List<FeeAdjustment> adjustments = feeAdjustmentRepository
                .findByStudentFeeUuidAndCollegeId(studentFeeUuid, collegeId);

        return StudentFeeMapper.toFeeAdjustmentResponseList(adjustments);
    }

    // ========== Summary and Reports ==========

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT', 'TEACHER', 'STUDENT')")
    public StudentFeeSummaryResponse getStudentFeeSummary(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        assertStudentAccess(studentUuid, collegeId);

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
                .map(this::getEffectiveNetAmount)
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
                .map(this::getEffectiveNetAmount)
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

    private void createInstallmentsForStudentFee(StudentFee studentFee, FeeStructure feeStructure) {
        if (feeStructure.getInstallmentTemplates() == null || feeStructure.getInstallmentTemplates().isEmpty()) {
            return;
        }

        List<FeeInstallmentTemplate> templates = feeStructure.getInstallmentTemplates().stream()
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .collect(Collectors.toList());

        for (FeeInstallmentTemplate template : templates) {
            FeeInstallment installment = FeeInstallment.builder()
                    .studentFee(studentFee)
                    .name(template.getName())
                    .amount(template.getAmount())
                    .paidAmount(BigDecimal.ZERO)
                    .dueAmount(template.getAmount())
                    .status(InstallmentStatus.PENDING)
                    .dueDate(template.getDueDate())
                    .build();
            feeInstallmentRepository.save(installment);
            studentFee.getInstallments().add(installment);
        }
    }

    private void allocatePaymentToInstallments(StudentFee studentFee, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<FeeInstallment> installments = feeInstallmentRepository.findByStudentFeeId(studentFee.getId());
        if (installments.isEmpty()) {
            return;
        }

        BigDecimal remaining = paymentAmount;
        for (FeeInstallment installment : installments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (installment.getDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal apply = remaining.min(installment.getDueAmount());
            BigDecimal newPaid = installment.getPaidAmount().add(apply);
            BigDecimal newDue = installment.getAmount().subtract(newPaid);

            installment.setPaidAmount(newPaid);
            installment.setDueAmount(newDue);
            installment.setStatus(calculateInstallmentStatus(installment.getAmount(), newPaid, newDue, installment.getDueDate()));
            feeInstallmentRepository.save(installment);

            remaining = remaining.subtract(apply);
        }
    }

    private InstallmentStatus calculateInstallmentStatus(BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal dueAmount, LocalDate dueDate) {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (dueAmount == null) {
            dueAmount = totalAmount;
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return InstallmentStatus.PENDING;
        }

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                return InstallmentStatus.OVERDUE;
            }
            return InstallmentStatus.PENDING;
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            return InstallmentStatus.PAID;
        } else if (dueAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                return InstallmentStatus.OVERDUE;
            }
            return InstallmentStatus.PARTIALLY_PAID;
        } else {
            return InstallmentStatus.PARTIALLY_PAID;
        }
    }

    private void applyAdjustmentToStudentFee(StudentFee studentFee, AdjustmentType type, BigDecimal amount) {
        if (type == null || amount == null) {
            return;
        }

        switch (type) {
            case DISCOUNT -> studentFee.setDiscountAmount(studentFee.getDiscountAmount().add(amount));
            case WAIVER -> studentFee.setWaiverAmount(studentFee.getWaiverAmount().add(amount));
            case PENALTY -> studentFee.setPenaltyAmount(studentFee.getPenaltyAmount().add(amount));
            default -> {
            }
        }

        recalculateStudentFeeTotals(studentFee);
    }

    private void recalculateStudentFeeTotals(StudentFee studentFee) {
        BigDecimal total = studentFee.getTotalAmount() != null ? studentFee.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal discount = studentFee.getDiscountAmount() != null ? studentFee.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal waiver = studentFee.getWaiverAmount() != null ? studentFee.getWaiverAmount() : BigDecimal.ZERO;
        BigDecimal penalty = studentFee.getPenaltyAmount() != null ? studentFee.getPenaltyAmount() : BigDecimal.ZERO;

        BigDecimal net = total.subtract(discount).subtract(waiver).add(penalty);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO;
        }
        studentFee.setNetAmount(net);

        // Adjust installments to match net amount if needed
        List<FeeInstallment> installments = feeInstallmentRepository.findByStudentFeeId(studentFee.getId());
        if (!installments.isEmpty()) {
            BigDecimal installmentSum = installments.stream()
                    .map(FeeInstallment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal diff = net.subtract(installmentSum);
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                FeeInstallment last = installments.get(installments.size() - 1);
                BigDecimal newAmount = last.getAmount().add(diff);
                if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResourceConflictException("Adjustments reduce installment below zero");
                }
                last.setAmount(newAmount);
                BigDecimal newDue = newAmount.subtract(last.getPaidAmount());
                if (newDue.compareTo(BigDecimal.ZERO) < 0) {
                    newDue = BigDecimal.ZERO;
                }
                last.setDueAmount(newDue);
                last.setStatus(calculateInstallmentStatus(newAmount, last.getPaidAmount(), newDue, last.getDueDate()));
                feeInstallmentRepository.save(last);
            }
        }

        BigDecimal paid = studentFee.getPaidAmount() != null ? studentFee.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal due = net.subtract(paid);
        if (due.compareTo(BigDecimal.ZERO) < 0) {
            due = BigDecimal.ZERO;
        }
        studentFee.setDueAmount(due);

        FeeStatus newStatus = StudentFeeMapper.calculateFeeStatus(net, paid, due, studentFee.getDueDate());
        studentFee.setStatus(newStatus);

        studentFeeRepository.save(studentFee);
    }

    private String generateReceiptNumber() {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FEE-" + timestamp + "-" + suffix;
    }

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        } catch (Exception e) {
            log.debug("Could not get current user: {}", e.getMessage());
        }
        return null;
    }

    private boolean isStudentRole() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> RoleType.ROLE_STUDENT.name().equals(a.getAuthority()));
        } catch (Exception e) {
            return false;
        }
    }

    private Student getCurrentStudent(Long collegeId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return studentRepository.findByUserIdAndCollegeId(currentUser.getId(), collegeId).orElse(null);
    }

    private void assertStudentAccess(String studentUuid, Long collegeId) {
        if (!isStudentRole()) {
            return;
        }
        Student currentStudent = getCurrentStudent(collegeId);
        if (currentStudent == null || !currentStudent.getUuid().equals(studentUuid)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void assertStudentFeeAccess(StudentFee studentFee, Long collegeId) {
        if (!isStudentRole()) {
            return;
        }
        Student currentStudent = getCurrentStudent(collegeId);
        if (currentStudent == null || studentFee.getStudent() == null
                || !studentFee.getStudent().getId().equals(currentStudent.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private BigDecimal getEffectiveNetAmount(StudentFee studentFee) {
        if (studentFee == null) {
            return BigDecimal.ZERO;
        }
        if (studentFee.getNetAmount() != null) {
            return studentFee.getNetAmount();
        }
        return studentFee.getTotalAmount() != null ? studentFee.getTotalAmount() : BigDecimal.ZERO;
    }
}

