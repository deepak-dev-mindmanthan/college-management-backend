package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.admission.*;
import org.collegemanagement.dto.student.StudentResponse;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.admission.AdmissionApplication;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.AdmissionStatus;
import org.collegemanagement.enums.EnrollmentStatus;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.AdmissionMapper;
import org.collegemanagement.mapper.StudentMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.AdmissionService;
import org.collegemanagement.services.UserManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionApplicationRepository admissionApplicationRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final ClassRoomRepository classRoomRepository;
    private final StudentRepository studentRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse createAdmission(CreateAdmissionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Generate application number
        String applicationNo = generateApplicationNumber(collegeId);

        // Find class if provided
        ClassRoom appliedClass = null;
        if (request.getClassUuid() != null && !request.getClassUuid().isBlank()) {
            appliedClass = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));
        }

        // Create admission application
        AdmissionApplication application = AdmissionApplication.builder()
                .college(college)
                .applicationNo(applicationNo)
                .studentName(request.getStudentName())
                .dob(request.getDob())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .appliedClass(appliedClass)
                .previousSchool(request.getPreviousSchool())
                .documentsJson(request.getDocumentsJson())
                .status(AdmissionStatus.DRAFT)
                .build();

        application = admissionApplicationRepository.save(application);

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse updateAdmission(String admissionUuid, UpdateAdmissionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find admission application
        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Only allow updates if in DRAFT status
        if (application.getStatus() != AdmissionStatus.DRAFT) {
            throw new ResourceConflictException("Cannot update admission application. Only DRAFT applications can be updated.");
        }

        // Update fields
        if (request.getStudentName() != null) {
            application.setStudentName(request.getStudentName());
        }
        if (request.getDob() != null) {
            application.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            application.setGender(request.getGender());
        }
        if (request.getEmail() != null) {
            application.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            application.setPhone(request.getPhone());
        }
        if (request.getClassUuid() != null) {
            if (request.getClassUuid().isBlank()) {
                application.setAppliedClass(null);
            } else {
                ClassRoom appliedClass = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));
                application.setAppliedClass(appliedClass);
            }
        }
        if (request.getPreviousSchool() != null) {
            application.setPreviousSchool(request.getPreviousSchool());
        }
        if (request.getDocumentsJson() != null) {
            application.setDocumentsJson(request.getDocumentsJson());
        }

        application = admissionApplicationRepository.save(application);

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse getAdmissionByUuid(String admissionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AdmissionResponse> getAllAdmissions(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<AdmissionApplication> applications = admissionApplicationRepository.findAllByCollegeId(collegeId, pageable);

        return applications.map(AdmissionMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AdmissionResponse> searchAdmissions(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<AdmissionApplication> applications = admissionApplicationRepository.searchApplicationsByCollegeId(collegeId, searchTerm, pageable);

        return applications.map(AdmissionMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AdmissionResponse> getAdmissionsByStatus(AdmissionStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<AdmissionApplication> applications = admissionApplicationRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return applications.map(AdmissionMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AdmissionResponse> getAdmissionsByClass(String classUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<AdmissionApplication> applications = admissionApplicationRepository.findByClassUuidAndCollegeId(classUuid, collegeId, pageable);

        return applications.map(AdmissionMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse submitAdmission(String admissionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Only allow submission if in DRAFT status
        if (application.getStatus() != AdmissionStatus.DRAFT) {
            throw new ResourceConflictException("Cannot submit admission application. Application is not in DRAFT status.");
        }

        // Update status to SUBMITTED
        application.setStatus(AdmissionStatus.SUBMITTED);
        application.setSubmittedAt(Instant.now());

        application = admissionApplicationRepository.save(application);

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse verifyAdmission(String admissionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Only allow verification if in SUBMITTED status
        if (application.getStatus() != AdmissionStatus.SUBMITTED) {
            throw new ResourceConflictException("Cannot verify admission application. Application must be in SUBMITTED status.");
        }

        // Update status to VERIFIED
        application.setStatus(AdmissionStatus.VERIFIED);

        application = admissionApplicationRepository.save(application);

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentResponse approveAdmission(String admissionUuid, ApproveAdmissionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Only allow approval if in VERIFIED status
        if (application.getStatus() != AdmissionStatus.VERIFIED) {
            throw new ResourceConflictException("Cannot approve admission application. Application must be in VERIFIED status.");
        }

        // Validate email uniqueness
        if (userManager.exitsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User with email " + request.getEmail() + " already exists");
        }

        // Validate roll number uniqueness within college
        if (studentRepository.existsByRollNumberAndCollegeId(request.getRollNumber(), collegeId)) {
            throw new ResourceConflictException("Student with roll number " + request.getRollNumber() + " already exists in this college");
        }

        // Validate registration number uniqueness within college
        if (studentRepository.existsByRegistrationNumberAndCollegeId(request.getRegistrationNumber(), collegeId)) {
            throw new ResourceConflictException("Student with registration number " + request.getRegistrationNumber() + " already exists in this college");
        }

        College college = application.getCollege();

        // Get student role
        java.util.Set<Role> studentRoles = roleService.getRoles(RoleType.ROLE_STUDENT);

        // Create user
        User user = User.builder()
                .name(application.getStudentName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(studentRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(user);
        User createdUser = userManager.findByEmail(request.getEmail());

        // Convert LocalDate to Instant for dob (assuming start of day)
        Instant dobInstant = application.getDob().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();

        // Create student
        Student student = Student.builder()
                .college(college)
                .user(createdUser)
                .rollNumber(request.getRollNumber())
                .registrationNumber(request.getRegistrationNumber())
                .dob(dobInstant)
                .gender(application.getGender())
                .admissionDate(request.getAdmissionDate())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .status(Status.ACTIVE)
                .build();

        student = studentRepository.save(student);

        // Optionally create enrollment if class and academic year are provided
        if (application.getAppliedClass() != null) {
            AcademicYear academicYear;
            if (request.getAcademicYearUuid() != null && !request.getAcademicYearUuid().isBlank()) {
                academicYear = academicYearRepository.findByUuidAndCollegeId(request.getAcademicYearUuid(), collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + request.getAcademicYearUuid()));
            } else {
                // Use active academic year if not specified
                academicYear = academicYearRepository.findActiveByCollegeId(collegeId)
                        .orElse(null);
            }

            if (academicYear != null) {
                // Check if enrollment already exists
                if (!studentEnrollmentRepository.existsByStudentIdAndAcademicYearId(student.getId(), academicYear.getId())) {
                    StudentEnrollment enrollment = StudentEnrollment.builder()
                            .college(college)
                            .student(student)
                            .academicYear(academicYear)
                            .classRoom(application.getAppliedClass())
                            .rollNumber(request.getRollNumber())
                            .status(EnrollmentStatus.ACTIVE)
                            .build();

                    studentEnrollmentRepository.save(enrollment);
                }
            }
        }

        // Update admission application status to APPROVED
        application.setStatus(AdmissionStatus.APPROVED);
        admissionApplicationRepository.save(application);

        return StudentMapper.toResponse(student);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionResponse rejectAdmission(String admissionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Cannot reject if already approved or rejected
        if (application.getStatus() == AdmissionStatus.APPROVED) {
            throw new ResourceConflictException("Cannot reject admission application. Application is already APPROVED.");
        }
        if (application.getStatus() == AdmissionStatus.REJECTED) {
            throw new ResourceConflictException("Admission application is already REJECTED.");
        }

        // Update status to REJECTED
        application.setStatus(AdmissionStatus.REJECTED);

        application = admissionApplicationRepository.save(application);

        return AdmissionMapper.toResponse(application);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteAdmission(String admissionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AdmissionApplication application = admissionApplicationRepository.findByUuidAndCollegeId(admissionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission application not found with UUID: " + admissionUuid));

        // Only allow deletion if in DRAFT status
        if (application.getStatus() != AdmissionStatus.DRAFT) {
            throw new ResourceConflictException("Cannot delete admission application. Only DRAFT applications can be deleted.");
        }

        admissionApplicationRepository.delete(application);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AdmissionSummary getAdmissionSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalApplications = admissionApplicationRepository.countByCollegeId(collegeId);
        long draftApplications = admissionApplicationRepository.countByStatusAndCollegeId(AdmissionStatus.DRAFT, collegeId);
        long submittedApplications = admissionApplicationRepository.countByStatusAndCollegeId(AdmissionStatus.SUBMITTED, collegeId);
        long verifiedApplications = admissionApplicationRepository.countByStatusAndCollegeId(AdmissionStatus.VERIFIED, collegeId);
        long approvedApplications = admissionApplicationRepository.countByStatusAndCollegeId(AdmissionStatus.APPROVED, collegeId);
        long rejectedApplications = admissionApplicationRepository.countByStatusAndCollegeId(AdmissionStatus.REJECTED, collegeId);

        return AdmissionSummary.builder()
                .totalApplications(totalApplications)
                .draftApplications(draftApplications)
                .submittedApplications(submittedApplications)
                .verifiedApplications(verifiedApplications)
                .approvedApplications(approvedApplications)
                .rejectedApplications(rejectedApplications)
                .build();
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        // Validate that the college belongs to the current tenant
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }

    private String generateApplicationNumber(Long collegeId) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String applicationNo = "APP-" + datePrefix + "-" + uniqueSuffix;

        // Ensure uniqueness
        int attempts = 0;
        while (admissionApplicationRepository.existsByApplicationNoAndCollegeId(applicationNo, collegeId) && attempts < 10) {
            uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            applicationNo = "APP-" + datePrefix + "-" + uniqueSuffix;
            attempts++;
        }

        if (attempts >= 10) {
            throw new IllegalStateException("Failed to generate unique application number");
        }

        return applicationNo;
    }
}

