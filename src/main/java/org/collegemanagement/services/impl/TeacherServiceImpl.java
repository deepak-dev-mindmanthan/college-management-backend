package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.dto.teacher.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.ClassSubjectTeacher;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.TeacherMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.TeacherService;
import org.collegemanagement.services.UserManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final TimetableRepository timetableRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final org.collegemanagement.services.CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (teacherRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Teacher with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get teacher role
        Set<Role> teacherRoles = roleService.getRoles(RoleType.ROLE_TEACHER);

        // Create user
        User teacher = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(teacherRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        // UserManager.createUser returns UserDto, so we need to get the entity by email after saving
        userManager.createUser(teacher);
        User createdUser = userManager.findByEmail(request.getEmail());

        // Create staff profile
        StaffProfile staffProfile = StaffProfile.builder()
                .college(college)
                .user(createdUser)
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .joiningDate(request.getJoiningDate())
                .build();

        staffProfileRepository.save(staffProfile);

        return TeacherMapper.toResponse(createdUser, staffProfile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public TeacherResponse updateTeacher(String teacherUuid, UpdateTeacherRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find teacher
        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(teacher.getEmail())) {
            if (teacherRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, teacher.getId())) {
                throw new ResourceConflictException("Teacher with email " + request.getEmail() + " already exists in this college");
            }
            teacher.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            teacher.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            teacher.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedTeacher = userManager.update(teacher);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedTeacher.getId(), collegeId)
                .orElse(null);

        if (staffProfile != null) {
            if (request.getDesignation() != null) {
                staffProfile.setDesignation(request.getDesignation());
            }
            if (request.getSalary() != null) {
                staffProfile.setSalary(request.getSalary());
            }
            if (request.getJoiningDate() != null) {
                staffProfile.setJoiningDate(request.getJoiningDate());
            }
            staffProfileRepository.save(staffProfile);
        } else if (request.getDesignation() != null || request.getSalary() != null || request.getJoiningDate() != null) {
            // Create staff profile if it doesn't exist but update request has staff fields
            College college = getCollegeById(collegeId);
            StaffProfile newProfile = StaffProfile.builder()
                    .college(college)
                    .user(updatedTeacher)
                    .designation(request.getDesignation() != null ? request.getDesignation() : "Teacher")
                    .salary(request.getSalary() != null ? request.getSalary() : java.math.BigDecimal.ZERO)
                    .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : java.time.LocalDate.now())
                    .build();
            staffProfileRepository.save(newProfile);
            staffProfile = newProfile;
        }

        return TeacherMapper.toResponse(updatedTeacher, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public TeacherResponse getTeacherByUuid(String teacherUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(teacher.getId(), collegeId)
                .orElse(null);

        return TeacherMapper.toResponse(teacher, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public TeacherDetailResponse getTeacherDetailsByUuid(String teacherUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Load teaching assignments
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository
                .findByTeacherIdAndCollegeId(teacher.getId(), collegeId);

        // Set teaching assignments to teacher entity for mapper
        teacher.setTeachingAssignments(assignments.stream().collect(Collectors.toSet()));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(teacher.getId(), collegeId)
                .orElse(null);

        return TeacherMapper.toDetailResponse(teacher, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<TeacherResponse> getAllTeachers(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> teachers = teacherRepository.findAllTeachersByCollegeId(collegeId, pageable);

        return teachers.map(teacher -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(teacher.getId(), collegeId)
                    .orElse(null);
            return TeacherMapper.toResponse(teacher, staffProfile);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<TeacherResponse> searchTeachers(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> teachers = teacherRepository.searchTeachersByCollegeId(collegeId, searchTerm, pageable);

        return teachers.map(teacher -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(teacher.getId(), collegeId)
                    .orElse(null);
            return TeacherMapper.toResponse(teacher, staffProfile);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteTeacher(String teacherUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Check if teacher has any assignments
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository
                .findByTeacherIdAndCollegeId(teacher.getId(), collegeId);

        if (!assignments.isEmpty()) {
            throw new ResourceConflictException("Cannot delete teacher. Teacher has active class/subject assignments. Please remove assignments first.");
        }

        // Check if teacher is class teacher of any class
        // This would require a query to check ClassRoom.classTeacher
        // For now, we'll allow deletion and let database constraints handle it

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(teacher.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(teacher.getId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void assignClassSubject(String teacherUuid, AssignClassSubjectRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find teacher
        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Find class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Find subject
        Subject subject = subjectRepository.findByUuidAndCollegeId(request.getSubjectUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with UUID: " + request.getSubjectUuid()));

        // Verify subject belongs to the class
        if (!subject.getClassRoom().getId().equals(classRoom.getId())) {
            throw new ResourceConflictException("Subject does not belong to the specified class");
        }

        // Check if assignment already exists
        if (classSubjectTeacherRepository.existsByClassUuidAndSubjectUuidAndTeacherIdAndCollegeId(
                request.getClassUuid(), request.getSubjectUuid(), teacher.getId(), collegeId)) {
            throw new ResourceConflictException("Teacher is already assigned to this class and subject");
        }

        // Create assignment
        ClassSubjectTeacher assignment = ClassSubjectTeacher.builder()
                .classRoom(classRoom)
                .subject(subject)
                .teacher(teacher)
                .build();

        classSubjectTeacherRepository.save(assignment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void removeClassSubjectAssignment(String teacherUuid, AssignClassSubjectRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find teacher
        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Find and delete assignment
        ClassSubjectTeacher assignment = classSubjectTeacherRepository
                .findByClassUuidAndSubjectUuidAndTeacherIdAndCollegeId(
                        request.getClassUuid(),
                        request.getSubjectUuid(),
                        teacher.getId(),
                        collegeId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found for teacher, class, and subject"));

        classSubjectTeacherRepository.delete(assignment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void assignClassTeacher(String teacherUuid, AssignClassTeacherRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find teacher
        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Find class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Assign class teacher
        classRoom.setClassTeacher(teacher);
        classRoomRepository.save(classRoom);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void removeClassTeacher(String classUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Remove class teacher
        classRoom.setClassTeacher(null);
        classRoomRepository.save(classRoom);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public TeacherTimetableResponse getTeacherTimetable(String teacherUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find teacher
        User teacher = teacherRepository.findTeacherByUuidAndCollegeId(teacherUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with UUID: " + teacherUuid));

        // Get timetable
        List<org.collegemanagement.entity.timetable.Timetable> timetable = timetableRepository
                .findByTeacherIdAndCollegeId(teacher.getId(), collegeId);

        // Map to response
        List<TeacherTimetableResponse.TimetableSlot> slots = timetable.stream()
                .map(t -> TeacherTimetableResponse.TimetableSlot.builder()
                        .uuid(t.getUuid())
                        .dayOfWeek(t.getDayOfWeek())
                        .periodNumber(t.getPeriodNumber())
                        .classUuid(t.getClassRoom().getUuid())
                        .className(t.getClassRoom().getName())
                        .section(t.getClassRoom().getSection())
                        .subjectUuid(t.getSubject().getUuid())
                        .subjectName(t.getSubject().getName())
                        .subjectCode(t.getSubject().getCode())
                        .build())
                .collect(Collectors.toList());

        return TeacherTimetableResponse.builder()
                .teacherUuid(teacher.getUuid())
                .teacherName(teacher.getName())
                .timetable(slots)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<TeacherResponse> getTeachersByDepartment(Long departmentId, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<User> teachers = teacherRepository.findTeachersByDepartmentId(departmentId, collegeId);

        // Convert to page (simplified - in production, use proper pagination)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), teachers.size());
        List<User> pagedTeachers = teachers.subList(start, end);

        List<TeacherResponse> responses = pagedTeachers.stream()
                .map(teacher -> {
                    StaffProfile staffProfile = staffProfileRepository
                            .findByUserIdAndCollegeId(teacher.getId(), collegeId)
                            .orElse(null);
                    return TeacherMapper.toResponse(teacher, staffProfile);
                })
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                teachers.size()
        );
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        // Validate that the college belongs to the current tenant
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

