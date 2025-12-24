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
import org.collegemanagement.dto.teacher.*;
import org.collegemanagement.services.TeacherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teachers")
@AllArgsConstructor
@Tag(name = "Teacher Management", description = "APIs for managing teachers in the college management system")
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(
            summary = "Create a new teacher",
            description = "Creates a new teacher with staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Teacher created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Teacher with email already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        TeacherResponse teacher = teacherService.createTeacher(request);
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher created successfully"));
    }

    @Operation(
            summary = "Update teacher information",
            description = "Updates teacher details including staff profile. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{teacherUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @Parameter(description = "UUID of the teacher to update")
            @PathVariable String teacherUuid,
            @Valid @RequestBody UpdateTeacherRequest request
    ) {
        TeacherResponse teacher = teacherService.updateTeacher(teacherUuid, request);
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher updated successfully"));
    }

    @Operation(
            summary = "Get teacher by UUID",
            description = "Retrieves teacher information by UUID. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the teacher themselves."
    )
    @GetMapping("/{teacherUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacher(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid
    ) {
        TeacherResponse teacher = teacherService.getTeacherByUuid(teacherUuid);
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher retrieved successfully"));
    }

    @Operation(
            summary = "Get teacher details",
            description = "Retrieves detailed teacher information including assigned classes, subjects, and departments."
    )
    @GetMapping("/{teacherUuid}/details")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDetailResponse>> getTeacherDetails(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid
    ) {
        TeacherDetailResponse teacher = teacherService.getTeacherDetailsByUuid(teacherUuid);
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher details retrieved successfully"));
    }

    @Operation(
            summary = "Get all teachers",
            description = "Retrieves a paginated list of all teachers in the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> getAllTeachers(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TeacherResponse> teachers = teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(ApiResponse.success(teachers, "Teachers retrieved successfully"));
    }

    @Operation(
            summary = "Search teachers",
            description = "Searches teachers by name or email. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> searchTeachers(
            @Parameter(description = "Search term (name or email)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TeacherResponse> teachers = teacherService.searchTeachers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(teachers, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Delete teacher",
            description = "Deletes a teacher. Teacher must not have any active class/subject assignments. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{teacherUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(
            @Parameter(description = "UUID of the teacher to delete")
            @PathVariable String teacherUuid
    ) {
        teacherService.deleteTeacher(teacherUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher deleted successfully"));
    }

    @Operation(
            summary = "Assign teacher to class and subject",
            description = "Assigns a teacher to teach a specific subject in a specific class. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{teacherUuid}/assign-class-subject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignClassSubject(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid,
            @Valid @RequestBody AssignClassSubjectRequest request
    ) {
        teacherService.assignClassSubject(teacherUuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher assigned to class and subject successfully"));
    }

    @Operation(
            summary = "Remove teacher assignment from class and subject",
            description = "Removes a teacher's assignment from a specific class and subject. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{teacherUuid}/assign-class-subject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeClassSubjectAssignment(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid,
            @Valid @RequestBody AssignClassSubjectRequest request
    ) {
        teacherService.removeClassSubjectAssignment(teacherUuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher assignment removed successfully"));
    }

    @Operation(
            summary = "Assign teacher as class teacher",
            description = "Assigns a teacher as the class teacher (tutor) for a specific class. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{teacherUuid}/assign-class-teacher")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignClassTeacher(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid,
            @Valid @RequestBody AssignClassTeacherRequest request
    ) {
        teacherService.assignClassTeacher(teacherUuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher assigned as class teacher successfully"));
    }

    @Operation(
            summary = "Remove class teacher assignment",
            description = "Removes a teacher's assignment as class teacher from a specific class. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/classes/{classUuid}/class-teacher")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeClassTeacher(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid
    ) {
        teacherService.removeClassTeacher(classUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Class teacher assignment removed successfully"));
    }

    @Operation(
            summary = "Get teacher timetable",
            description = "Retrieves the complete timetable for a teacher. Accessible by COLLEGE_ADMIN, SUPER_ADMIN, or the teacher themselves."
    )
    @GetMapping("/{teacherUuid}/timetable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<TeacherTimetableResponse>> getTeacherTimetable(
            @Parameter(description = "UUID of the teacher")
            @PathVariable String teacherUuid
    ) {
        TeacherTimetableResponse timetable = teacherService.getTeacherTimetable(teacherUuid);
        return ResponseEntity.ok(ApiResponse.success(timetable, "Teacher timetable retrieved successfully"));
    }

    @Operation(
            summary = "Get teachers by department",
            description = "Retrieves all teachers who are heads of a specific department. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TeacherResponse>>> getTeachersByDepartment(
            @Parameter(description = "ID of the department")
            @PathVariable Long departmentId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TeacherResponse> teachers = teacherService.getTeachersByDepartment(departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(teachers, "Department teachers retrieved successfully"));
    }
}
