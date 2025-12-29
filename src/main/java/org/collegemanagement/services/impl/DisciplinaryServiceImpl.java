package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.discipline.CreateDisciplinaryCaseRequest;
import org.collegemanagement.dto.discipline.DisciplinaryCaseResponse;
import org.collegemanagement.dto.discipline.UpdateDisciplinaryCaseRequest;
import org.collegemanagement.entity.discipline.DisciplinaryCase;
import org.collegemanagement.entity.student.ParentStudent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.DisciplinaryStatus;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.DisciplinaryCaseMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.DisciplinaryService;
import org.collegemanagement.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisciplinaryServiceImpl implements DisciplinaryService {

    private final DisciplinaryCaseRepository disciplinaryCaseRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final StudentRepository studentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public DisciplinaryCaseResponse createDisciplinaryCase(CreateDisciplinaryCaseRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Get current user (reporter)
        User reportedBy = getCurrentUser();
        if (reportedBy == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Create disciplinary case
        DisciplinaryCase disciplinaryCase = DisciplinaryCase.builder()
                .college(college)
                .student(student)
                .reportedBy(reportedBy)
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .actionTaken(null)
                .status(DisciplinaryStatus.REPORTED)
                .build();

        disciplinaryCase = disciplinaryCaseRepository.save(disciplinaryCase);

        // Send notifications to student
        if (student.getUser() != null && student.getUser().getId() != null) {
            try {
                notificationService.createNotification(
                        student.getUser().getId(),
                        "Disciplinary Case Reported",
                        "A disciplinary case has been reported for you. Please check with the administration.",
                        NotificationType.IN_APP,
                        null, // NotificationReferenceType doesn't have DISCIPLINARY_CASE yet
                        disciplinaryCase.getId(),
                        "/disciplinary-cases/" + disciplinaryCase.getUuid(),
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
                            "Disciplinary Case: " + student.getUser().getName(),
                            "A disciplinary case has been reported for your child " + student.getUser().getName()
                                    + " (" + student.getRollNumber() + "). Please check with the administration.",
                            NotificationType.IN_APP,
                            null,
                            disciplinaryCase.getId(),
                            "/disciplinary-cases/" + disciplinaryCase.getUuid(),
                            10 // High priority
                    );
                } catch (Exception e) {
                    log.warn("Failed to send notification to parent: {}", e.getMessage());
                }
            }
        }

        return DisciplinaryCaseMapper.toResponse(disciplinaryCase);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public DisciplinaryCaseResponse updateDisciplinaryCase(String caseUuid, UpdateDisciplinaryCaseRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find disciplinary case
        DisciplinaryCase disciplinaryCase = disciplinaryCaseRepository.findByUuidAndCollegeId(caseUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplinary case not found with UUID: " + caseUuid));

        // Update fields
        if (request.getIncidentDate() != null) {
            disciplinaryCase.setIncidentDate(request.getIncidentDate());
        }
        if (request.getDescription() != null) {
            disciplinaryCase.setDescription(request.getDescription());
        }
        if (request.getActionTaken() != null) {
            disciplinaryCase.setActionTaken(request.getActionTaken());
        }
        if (request.getStatus() != null) {
            disciplinaryCase.setStatus(request.getStatus());

            // Send notifications on status change
            Student student = disciplinaryCase.getStudent();
            if (student != null && student.getUser() != null && student.getUser().getId() != null) {
                try {
                    String statusMessage = switch (request.getStatus()) {
                        case UNDER_REVIEW -> "Your disciplinary case is under review.";
                        case ACTION_TAKEN -> "Action has been taken on your disciplinary case. " +
                                (disciplinaryCase.getActionTaken() != null ? "Action: " + disciplinaryCase.getActionTaken() : "");
                        case CLOSED -> "Your disciplinary case has been closed.";
                        default -> "Your disciplinary case status has been updated.";
                    };

                    notificationService.createNotification(
                            student.getUser().getId(),
                            "Disciplinary Case Update",
                            statusMessage,
                            NotificationType.IN_APP,
                            null,
                            disciplinaryCase.getId(),
                            "/disciplinary-cases/" + disciplinaryCase.getUuid(),
                            10
                    );

                    // Notify parents
                    List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());
                    for (ParentStudent parentStudent : parentStudents) {
                        if (parentStudent.getParent() != null && parentStudent.getParent().getUser() != null
                                && parentStudent.getParent().getUser().getId() != null) {
                            notificationService.createNotification(
                                    parentStudent.getParent().getUser().getId(),
                                    "Disciplinary Case Update: " + student.getUser().getName(),
                                    "The disciplinary case for your child " + student.getUser().getName()
                                            + " has been updated. " + statusMessage,
                                    NotificationType.IN_APP,
                                    null,
                                    disciplinaryCase.getId(),
                                    "/disciplinary-cases/" + disciplinaryCase.getUuid(),
                                    10
                            );
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to send notification: {}", e.getMessage());
                }
            }
        }

        disciplinaryCase = disciplinaryCaseRepository.save(disciplinaryCase);

        return DisciplinaryCaseMapper.toResponse(disciplinaryCase);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public DisciplinaryCaseResponse getDisciplinaryCaseByUuid(String caseUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        DisciplinaryCase disciplinaryCase = disciplinaryCaseRepository.findByUuidAndCollegeId(caseUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplinary case not found with UUID: " + caseUuid));

        return DisciplinaryCaseMapper.toResponse(disciplinaryCase);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<DisciplinaryCaseResponse> getAllDisciplinaryCases(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<DisciplinaryCase> cases = disciplinaryCaseRepository.findAllByCollegeId(collegeId, pageable);

        return cases.map(DisciplinaryCaseMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStudent(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<DisciplinaryCase> cases = disciplinaryCaseRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId, pageable);

        return cases.map(DisciplinaryCaseMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStatus(DisciplinaryStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<DisciplinaryCase> cases = disciplinaryCaseRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return cases.map(DisciplinaryCaseMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStudentAndStatus(
            String studentUuid, DisciplinaryStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<DisciplinaryCase> cases = disciplinaryCaseRepository.findByStudentUuidAndStatusAndCollegeId(
                studentUuid, status, collegeId, pageable);

        return cases.map(DisciplinaryCaseMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<DisciplinaryCaseResponse> getDisciplinaryCasesByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<DisciplinaryCase> cases = disciplinaryCaseRepository.findByDateRangeAndCollegeId(
                collegeId, startDate, endDate, pageable);

        return cases.map(DisciplinaryCaseMapper::toResponse);
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

