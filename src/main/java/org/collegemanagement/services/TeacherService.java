package org.collegemanagement.services;

import org.collegemanagement.dto.teacher.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherService {

    /**
     * Create a new teacher
     */
    TeacherResponse createTeacher(CreateTeacherRequest request);

    /**
     * Update teacher information
     */
    TeacherResponse updateTeacher(String teacherUuid, UpdateTeacherRequest request);

    /**
     * Get teacher by UUID
     */
    TeacherResponse getTeacherByUuid(String teacherUuid);

    /**
     * Get teacher details with additional information
     */
    TeacherDetailResponse getTeacherDetailsByUuid(String teacherUuid);

    /**
     * Get all teachers with pagination
     */
    Page<TeacherResponse> getAllTeachers(Pageable pageable);

    /**
     * Search teachers by name or email
     */
    Page<TeacherResponse> searchTeachers(String searchTerm, Pageable pageable);

    /**
     * Delete teacher by UUID
     */
    void deleteTeacher(String teacherUuid);

    /**
     * Assign teacher to a class and subject
     */
    void assignClassSubject(String teacherUuid, AssignClassSubjectRequest request);

    /**
     * Remove teacher assignment from a class and subject
     */
    void removeClassSubjectAssignment(String teacherUuid, AssignClassSubjectRequest request);

    /**
     * Assign teacher as class teacher
     */
    void assignClassTeacher(String teacherUuid, AssignClassTeacherRequest request);

    /**
     * Remove teacher as class teacher
     */
    void removeClassTeacher(String classUuid);

    /**
     * Get teacher's timetable
     */
    TeacherTimetableResponse getTeacherTimetable(String teacherUuid);

    /**
     * Get teachers by department
     */
    Page<TeacherResponse> getTeachersByDepartment(Long departmentId, Pageable pageable);
}

