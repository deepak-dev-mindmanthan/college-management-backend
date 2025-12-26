package org.collegemanagement.services;

import org.collegemanagement.dto.student.*;
import org.collegemanagement.enums.EnrollmentStatus;
import org.collegemanagement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    /**
     * Create a new student
     */
    StudentResponse createStudent(CreateStudentRequest request);

    /**
     * Update student information
     */
    StudentResponse updateStudent(String studentUuid, UpdateStudentRequest request);

    /**
     * Get student by UUID
     */
    StudentResponse getStudentByUuid(String studentUuid);

    /**
     * Get student details with additional information (parents, enrollments, etc.)
     */
    StudentDetailResponse getStudentDetailsByUuid(String studentUuid);

    /**
     * Get all students with pagination
     */
    Page<StudentResponse> getAllStudents(Pageable pageable);

    /**
     * Search students by name, roll number, registration number, or email
     */
    Page<StudentResponse> searchStudents(String searchTerm, Pageable pageable);

    /**
     * Get students by status
     */
    Page<StudentResponse> getStudentsByStatus(Status status, Pageable pageable);

    /**
     * Get students by class UUID
     */
    Page<StudentResponse> getStudentsByClass(String classUuid, Pageable pageable);

    /**
     * Get students by academic year UUID
     */
    Page<StudentResponse> getStudentsByAcademicYear(String academicYearUuid, Pageable pageable);

    /**
     * Delete student by UUID
     */
    void deleteStudent(String studentUuid);

    /**
     * Assign parent to student
     */
    void assignParent(String studentUuid, AssignParentRequest request);

    /**
     * Remove parent from student
     */
    void removeParent(String studentUuid, String parentUuid);

    /**
     * Create enrollment for student
     */
    void createEnrollment(String studentUuid, CreateEnrollmentRequest request);

    /**
     * Update enrollment status
     */
    void updateEnrollmentStatus(String studentUuid, String enrollmentUuid, EnrollmentStatus status);

    /**
     * Get student summary statistics
     */
    StudentSummary getStudentSummary();
}

