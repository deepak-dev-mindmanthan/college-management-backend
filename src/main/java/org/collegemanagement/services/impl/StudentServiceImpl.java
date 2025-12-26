package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.student.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.student.Parent;
import org.collegemanagement.entity.student.ParentStudent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.EnrollmentStatus;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.StudentMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.StudentService;
import org.collegemanagement.services.StudentSummary;
import org.collegemanagement.services.UserManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentResponse createStudent(CreateStudentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

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

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get student role
        java.util.Set<Role> studentRoles = roleService.getRoles(RoleType.ROLE_STUDENT);

        // Create user
        User user = User.builder()
                .name(request.getName())
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

        // Create student
        Student student = Student.builder()
                .college(college)
                .user(createdUser)
                .rollNumber(request.getRollNumber())
                .registrationNumber(request.getRegistrationNumber())
                .dob(request.getDob())
                .gender(request.getGender())
                .admissionDate(request.getAdmissionDate())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .status(Status.ACTIVE)
                .build();

        student = studentRepository.save(student);

        return StudentMapper.toResponse(student);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentResponse updateStudent(String studentUuid, UpdateStudentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        User user = student.getUser();

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userManager.exitsByEmail(request.getEmail())) {
                throw new ResourceConflictException("User with email " + request.getEmail() + " already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        userManager.update(user);

        // Update roll number if provided
        if (request.getRollNumber() != null && !request.getRollNumber().equals(student.getRollNumber())) {
            if (studentRepository.existsByRollNumberAndCollegeIdAndIdNot(request.getRollNumber(), collegeId, student.getId())) {
                throw new ResourceConflictException("Student with roll number " + request.getRollNumber() + " already exists in this college");
            }
            student.setRollNumber(request.getRollNumber());
        }

        // Update registration number if provided
        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().equals(student.getRegistrationNumber())) {
            if (studentRepository.existsByRegistrationNumberAndCollegeIdAndIdNot(request.getRegistrationNumber(), collegeId, student.getId())) {
                throw new ResourceConflictException("Student with registration number " + request.getRegistrationNumber() + " already exists in this college");
            }
            student.setRegistrationNumber(request.getRegistrationNumber());
        }

        // Update other fields
        if (request.getDob() != null) {
            student.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getAdmissionDate() != null) {
            student.setAdmissionDate(request.getAdmissionDate());
        }
        if (request.getBloodGroup() != null) {
            student.setBloodGroup(request.getBloodGroup());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }

        student = studentRepository.save(student);

        return StudentMapper.toResponse(student);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public StudentResponse getStudentByUuid(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        return StudentMapper.toResponse(student);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public StudentDetailResponse getStudentDetailsByUuid(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Load parents
        List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());

        // Load enrollments
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByStudentIdAndCollegeId(student.getId(), collegeId);

        return StudentMapper.toDetailResponse(student, parentStudents, enrollments);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentResponse> getAllStudents(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Student> students = studentRepository.findAllByCollegeId(collegeId, pageable);

        return students.map(StudentMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentResponse> searchStudents(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Student> students = studentRepository.searchStudentsByCollegeId(collegeId, searchTerm, pageable);

        return students.map(StudentMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentResponse> getStudentsByStatus(Status status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Student> students = studentRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return students.map(StudentMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentResponse> getStudentsByClass(String classUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find class and validate
        classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Find enrollments for this class
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassUuidAndCollegeId(classUuid, collegeId);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrollments.size());
        List<StudentEnrollment> pagedEnrollments = enrollments.subList(start, end);

        List<StudentResponse> responses = pagedEnrollments.stream()
                .map(StudentEnrollment::getStudent)
                .map(StudentMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, enrollments.size());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentResponse> getStudentsByAcademicYear(String academicYearUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find academic year and validate
        academicYearRepository.findByUuidAndCollegeId(academicYearUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + academicYearUuid));

        // Find enrollments for this academic year
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByAcademicYearUuidAndCollegeId(academicYearUuid, collegeId);

        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrollments.size());
        List<StudentEnrollment> pagedEnrollments = enrollments.subList(start, end);

        List<StudentResponse> responses = pagedEnrollments.stream()
                .map(StudentEnrollment::getStudent)
                .map(StudentMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, enrollments.size());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Check if student has active enrollments
        List<StudentEnrollment> activeEnrollments = studentEnrollmentRepository
                .findByStudentIdAndCollegeId(student.getId(), collegeId)
                .stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .toList();

        if (!activeEnrollments.isEmpty()) {
            throw new ResourceConflictException("Cannot delete student. Student has active enrollments. Please remove enrollments first.");
        }

        // Delete parent-student relationships
        List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());
        parentStudentRepository.deleteAll(parentStudents);

        // Delete enrollments
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByStudentIdAndCollegeId(student.getId(), collegeId);
        studentEnrollmentRepository.deleteAll(enrollments);

        // Delete student
        studentRepository.delete(student);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(student.getUser().getId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void assignParent(String studentUuid, AssignParentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Find parent
        Parent parent = parentRepository.findByUuidAndCollegeId(request.getParentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with UUID: " + request.getParentUuid()));

        // Check if relationship already exists
        if (parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())) {
            throw new ResourceConflictException("Parent is already assigned to this student");
        }

        // Create parent-student relationship
        ParentStudent parentStudent = ParentStudent.builder()
                .parent(parent)
                .student(student)
                .relation(request.getRelation())
                .build();

        parentStudentRepository.save(parentStudent);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void removeParent(String studentUuid, String parentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Find parent
        Parent parent = parentRepository.findByUuidAndCollegeId(parentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with UUID: " + parentUuid));

        // Find and delete relationship
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parent.getId(), student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent-student relationship not found"));

        parentStudentRepository.delete(parentStudent);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void createEnrollment(String studentUuid, CreateEnrollmentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Find academic year
        AcademicYear academicYear = academicYearRepository.findByUuidAndCollegeId(request.getAcademicYearUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + request.getAcademicYearUuid()));

        // Find class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Check if enrollment already exists for this student and academic year
        if (studentEnrollmentRepository.existsByStudentIdAndAcademicYearId(student.getId(), academicYear.getId())) {
            throw new ResourceConflictException("Student is already enrolled for this academic year");
        }

        // Create enrollment
        StudentEnrollment enrollment = StudentEnrollment.builder()
                .college(student.getCollege())
                .student(student)
                .academicYear(academicYear)
                .classRoom(classRoom)
                .rollNumber(request.getRollNumber())
                .status(request.getStatus())
                .build();

        studentEnrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void updateEnrollmentStatus(String studentUuid, String enrollmentUuid, EnrollmentStatus status) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student (validate access)
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Find enrollment
        StudentEnrollment enrollment = studentEnrollmentRepository.findByUuidAndCollegeId(enrollmentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with UUID: " + enrollmentUuid));

        // Validate that enrollment belongs to the student
        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new ResourceConflictException("Enrollment does not belong to the specified student");
        }

        // Update status
        enrollment.setStatus(status);
        studentEnrollmentRepository.save(enrollment);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentSummary getStudentSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalStudents = studentRepository.countByCollegeId(collegeId);
        long activeStudents = studentRepository.countByStatusAndCollegeId(Status.ACTIVE, collegeId);
        long suspendedStudents = studentRepository.countByStatusAndCollegeId(Status.SUSPENDED, collegeId);

        return StudentSummary.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .suspendedStudents(suspendedStudents)
                .build();
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        // Validate that the college belongs to the current tenant
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

