package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.promotion.PromoteStudentRequest;
import org.collegemanagement.dto.promotion.StudentPromotionResponse;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.academic.StudentPromotionLog;
import org.collegemanagement.entity.student.ParentStudent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.EnrollmentStatus;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.StudentPromotionMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.NotificationService;
import org.collegemanagement.services.StudentPromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentPromotionServiceImpl implements StudentPromotionService {

    private final StudentPromotionLogRepository studentPromotionLogRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentPromotionResponse promoteStudent(PromoteStudentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Find academic year
        AcademicYear academicYear = academicYearRepository.findByUuidAndCollegeId(request.getAcademicYearUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + request.getAcademicYearUuid()));

        // Find to class
        ClassRoom toClass = classRoomRepository.findByUuidAndCollegeId(request.getToClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getToClassUuid()));

        // Find current active enrollment (from class)
        StudentEnrollment currentEnrollment = studentEnrollmentRepository
                .findActiveByStudentIdAndCollegeId(student.getId(), collegeId)
                .orElseThrow(() -> new ResourceConflictException(
                        "Student does not have an active enrollment. Cannot promote."));

        ClassRoom fromClass = currentEnrollment.getClassRoom();

        // Validate that from and to classes are different
        if (fromClass.getId().equals(toClass.getId())) {
            throw new ResourceConflictException("Cannot promote student to the same class. From and to classes must be different.");
        }

        // Check if enrollment already exists for the new academic year
        if (studentEnrollmentRepository.existsByStudentIdAndAcademicYearId(student.getId(), academicYear.getId())) {
            throw new ResourceConflictException(
                    "Student is already enrolled for academic year " + academicYear.getYearName());
        }

        // Get current user (promoter)
        User promotedBy = getCurrentUser();
        if (promotedBy == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Update old enrollment status to PROMOTED (optional, but good practice)
        currentEnrollment.setStatus(EnrollmentStatus.PROMOTED);
        studentEnrollmentRepository.save(currentEnrollment);

        // Create new enrollment for the new academic year and class
        StudentEnrollment newEnrollment = StudentEnrollment.builder()
                .college(college)
                .student(student)
                .academicYear(academicYear)
                .classRoom(toClass)
                .rollNumber(request.getRollNumber())
                .status(request.getEnrollmentStatus())
                .build();

        newEnrollment = studentEnrollmentRepository.save(newEnrollment);

        // Create promotion log
        StudentPromotionLog promotionLog = StudentPromotionLog.builder()
                .college(college)
                .student(student)
                .fromClass(fromClass)
                .toClass(toClass)
                .academicYear(academicYear)
                .promotedBy(promotedBy)
                .remarks(request.getRemarks())
                .build();

        promotionLog = studentPromotionLogRepository.save(promotionLog);

        // Send notifications to student
        if (student.getUser() != null && student.getUser().getId() != null) {
            try {
                notificationService.createNotification(
                        student.getUser().getId(),
                        "Promoted to " + toClass.getName(),
                        "You have been promoted from " + fromClass.getName() + " to " + toClass.getName()
                                + " for academic year " + academicYear.getYearName() + ".",
                        NotificationType.IN_APP,
                        null, // NotificationReferenceType doesn't have PROMOTION yet
                        promotionLog.getId(),
                        "/students/" + student.getUuid(),
                        10 // High priority
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to student: {}", e.getMessage());
            }
        }

        // Send notifications to parents
        List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());
        for (ParentStudent parentStudent : parentStudents) {
            if (parentStudent.getParent() != null && parentStudent.getParent().getUser() != null
                    && parentStudent.getParent().getUser().getId() != null) {
                try {
                    notificationService.createNotification(
                            parentStudent.getParent().getUser().getId(),
                            "Promotion: " + student.getUser().getName(),
                            "Your child " + student.getUser().getName() + " (" + student.getRollNumber()
                                    + ") has been promoted from " + fromClass.getName() + " to " + toClass.getName()
                                    + " for academic year " + academicYear.getYearName() + ".",
                            NotificationType.IN_APP,
                            null,
                            promotionLog.getId(),
                            "/students/" + student.getUuid(),
                            10
                    );
                } catch (Exception e) {
                    log.warn("Failed to send notification to parent: {}", e.getMessage());
                }
            }
        }

        return StudentPromotionMapper.toResponse(promotionLog);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public StudentPromotionResponse getPromotionLogByUuid(String promotionLogUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        StudentPromotionLog promotionLog = studentPromotionLogRepository.findByUuidAndCollegeId(promotionLogUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion log not found with UUID: " + promotionLogUuid));

        return StudentPromotionMapper.toResponse(promotionLog);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentPromotionResponse> getAllPromotionLogs(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<StudentPromotionLog> promotionLogs = studentPromotionLogRepository.findAllByCollegeId(collegeId, pageable);

        return promotionLogs.map(StudentPromotionMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public List<StudentPromotionResponse> getPromotionHistoryByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<StudentPromotionLog> promotionLogs = studentPromotionLogRepository
                .findAllByStudentUuidAndCollegeId(studentUuid, collegeId);

        return promotionLogs.stream()
                .map(StudentPromotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentPromotionResponse> getPromotionLogsByAcademicYear(String academicYearUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate academic year exists
        academicYearRepository.findByUuidAndCollegeId(academicYearUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + academicYearUuid));

        Page<StudentPromotionLog> promotionLogs = studentPromotionLogRepository
                .findByAcademicYearUuidAndCollegeId(academicYearUuid, collegeId, pageable);

        return promotionLogs.map(StudentPromotionMapper::toResponse);
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
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
}

