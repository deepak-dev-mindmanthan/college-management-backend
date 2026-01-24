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
import org.collegemanagement.dto.StudentSummary;
import org.collegemanagement.dto.student.*;
import org.collegemanagement.enums.EnrollmentStatus;
import org.collegemanagement.enums.Status;
import org.collegemanagement.services.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@AllArgsConstructor
@Tag(name = "Student Management", description = "APIs for managing students in the college management system")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Create a new student",
            description = "Creates a new student with user account. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Student with email/roll number/registration number already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        StudentResponse student = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(student, "Student created successfully",HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update student information",
            description = "Updates student details. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Parameter(description = "UUID of the student to update")
            @PathVariable String studentUuid,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        StudentResponse student = studentService.updateStudent(studentUuid, request);
        return ResponseEntity.ok(ApiResponse.success(student, "Student updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student by UUID",
            description = "Retrieves student information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, or the student themselves."
    )
    @GetMapping("/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        StudentResponse student = studentService.getStudentByUuid(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(student, "Student retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student details",
            description = "Retrieves detailed student information including parents and enrollments."
    )
    @GetMapping("/{studentUuid}/details")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentDetails(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        StudentDetailResponse student = studentService.getStudentDetailsByUuid(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(student, "Student details retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all students",
            description = "Retrieves a paginated list of all students in the college. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getAllStudents(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "admissionDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentResponse> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Search students",
            description = "Searches students by name, roll number, registration number, or email. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> searchStudents(
            @Parameter(description = "Search term (name, roll number, registration number, or email)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "rollNumber") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentResponse> students = studentService.searchStudents(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(students, "Search results retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get students by status",
            description = "Retrieves students filtered by status. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudentsByStatus(
            @Parameter(description = "Student status")
            @PathVariable Status status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> students = studentService.getStudentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get students by class",
            description = "Retrieves all students in a specific class. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/class/{classUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudentsByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> students = studentService.getStudentsByClass(classUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get students by academic year",
            description = "Retrieves all students enrolled in a specific academic year. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/academic-year/{academicYearUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudentsByAcademicYear(
            @Parameter(description = "UUID of the academic year")
            @PathVariable String academicYearUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentResponse> students = studentService.getStudentsByAcademicYear(academicYearUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete student",
            description = "Deletes a student. Student must not have any active enrollments. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "UUID of the student to delete")
            @PathVariable String studentUuid
    ) {
        studentService.deleteStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Assign parent to student",
            description = "Assigns a parent to a student with a relation type. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{studentUuid}/parents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignParent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Valid @RequestBody AssignParentRequest request
    ) {
        studentService.assignParent(studentUuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Parent assigned successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Remove parent from student",
            description = "Removes a parent-student relationship. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{studentUuid}/parents/{parentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeParent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "UUID of the parent")
            @PathVariable String parentUuid
    ) {
        studentService.removeParent(studentUuid, parentUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Parent removed successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Create enrollment for student",
            description = "Creates an enrollment for a student in a class for an academic year. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{studentUuid}/enrollments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createEnrollment(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Valid @RequestBody CreateEnrollmentRequest request
    ) {
        studentService.createEnrollment(studentUuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Enrollment created successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Update enrollment status",
            description = "Updates the status of a student enrollment (e.g., ACTIVE, PROMOTED, DROPPED, COMPLETED). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{studentUuid}/enrollments/{enrollmentUuid}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateEnrollmentStatus(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "UUID of the enrollment")
            @PathVariable String enrollmentUuid,
            @Parameter(description = "New enrollment status")
            @RequestParam EnrollmentStatus status
    ) {
        studentService.updateEnrollmentStatus(studentUuid, enrollmentUuid, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Enrollment status updated successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get student summary",
            description = "Retrieves student summary statistics. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentSummary>> getStudentSummary() {
        StudentSummary summary = studentService.getStudentSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Student summary retrieved successfully",HttpStatus.OK.value()));
    }
}

