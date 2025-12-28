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
import org.collegemanagement.dto.exam.*;
import org.collegemanagement.enums.ExamType;
import org.collegemanagement.services.ExamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@AllArgsConstructor
@Tag(name = "Exam Management", description = "APIs for managing exams, marks, grades, and transcripts in the college management system")
public class ExamController {

    private final ExamService examService;

    // ========== Exam Management Endpoints ==========

    @Operation(
            summary = "Create a new exam",
            description = "Creates a new exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Exam created successfully",
                    content = @Content(schema = @Schema(implementation = ExamResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Exam with name already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @Valid @RequestBody CreateExamRequest request
    ) {
        ExamResponse exam = examService.createExam(request);
        return ResponseEntity.ok(ApiResponse.success(exam, "Exam created successfully"));
    }

    @Operation(
            summary = "Update exam information",
            description = "Updates exam details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PutMapping("/{examUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @Parameter(description = "UUID of the exam to update")
            @PathVariable String examUuid,
            @Valid @RequestBody UpdateExamRequest request
    ) {
        ExamResponse exam = examService.updateExam(examUuid, request);
        return ResponseEntity.ok(ApiResponse.success(exam, "Exam updated successfully"));
    }

    @Operation(
            summary = "Get exam by UUID",
            description = "Retrieves exam information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/{examUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExamResponse>> getExam(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid
    ) {
        ExamResponse exam = examService.getExamByUuid(examUuid);
        return ResponseEntity.ok(ApiResponse.success(exam, "Exam retrieved successfully"));
    }

    @Operation(
            summary = "Get all exams",
            description = "Retrieves a paginated list of all exams. Accessible by all authenticated users."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<ExamResponse>>> getAllExams(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ExamResponse> exams = examService.getAllExams(pageable);
        return ResponseEntity.ok(ApiResponse.success(exams, "Exams retrieved successfully"));
    }

    @Operation(
            summary = "Get all exams (without pagination)",
            description = "Retrieves all exams as a list. Accessible by all authenticated users."
    )
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExamsList() {
        List<ExamResponse> exams = examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.success(exams, "Exams retrieved successfully"));
    }

    @Operation(
            summary = "Get exams by type",
            description = "Retrieves exams filtered by type. Accessible by all authenticated users."
    )
    @GetMapping("/type/{examType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<ExamResponse>>> getExamsByType(
            @Parameter(description = "Exam type (UNIT_TEST, MIDTERM, FINAL, INTERNAL, PRACTICAL)")
            @PathVariable ExamType examType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ExamResponse> exams = examService.getExamsByType(examType, pageable);
        return ResponseEntity.ok(ApiResponse.success(exams, "Exams retrieved successfully"));
    }

    @Operation(
            summary = "Get exams by academic year",
            description = "Retrieves exams filtered by academic year. Accessible by all authenticated users."
    )
    @GetMapping("/academic-year/{academicYearUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<ExamResponse>>> getExamsByAcademicYear(
            @Parameter(description = "UUID of the academic year")
            @PathVariable String academicYearUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ExamResponse> exams = examService.getExamsByAcademicYear(academicYearUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(exams, "Exams retrieved successfully"));
    }

    @Operation(
            summary = "Search exams",
            description = "Searches exams by name. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<ExamResponse>>> searchExams(
            @Parameter(description = "Search term (exam name)")
            @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ExamResponse> exams = examService.searchExams(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(exams, "Search results retrieved successfully"));
    }

    @Operation(
            summary = "Get exam summary",
            description = "Retrieves summary statistics for an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/{examUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamSummaryResponse>> getExamSummary(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid
    ) {
        ExamSummaryResponse summary = examService.getExamSummary(examUuid);
        return ResponseEntity.ok(ApiResponse.success(summary, "Exam summary retrieved successfully"));
    }

    @Operation(
            summary = "Delete exam",
            description = "Deletes an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @DeleteMapping("/{examUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(
            @Parameter(description = "UUID of the exam to delete")
            @PathVariable String examUuid
    ) {
        examService.deleteExam(examUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Exam deleted successfully"));
    }

    // ========== Exam Class Management Endpoints ==========

    @Operation(
            summary = "Add class to exam",
            description = "Adds a class to an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/{examUuid}/classes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamClassResponse>> addClassToExam(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid,
            @Valid @RequestBody AddClassToExamRequest request
    ) {
        ExamClassResponse examClass = examService.addClassToExam(examUuid, request);
        return ResponseEntity.ok(ApiResponse.success(examClass, "Class added to exam successfully"));
    }

    @Operation(
            summary = "Remove class from exam",
            description = "Removes a class from an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @DeleteMapping("/{examUuid}/classes/{examClassUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> removeClassFromExam(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid,
            @Parameter(description = "UUID of the exam class to remove")
            @PathVariable String examClassUuid
    ) {
        examService.removeClassFromExam(examUuid, examClassUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Class removed from exam successfully"));
    }

    @Operation(
            summary = "Get exam class by UUID",
            description = "Retrieves exam class information. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{examClassUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExamClassResponse>> getExamClass(
            @Parameter(description = "UUID of the exam class")
            @PathVariable String examClassUuid
    ) {
        ExamClassResponse examClass = examService.getExamClassByUuid(examClassUuid);
        return ResponseEntity.ok(ApiResponse.success(examClass, "Exam class retrieved successfully"));
    }

    // ========== Exam Subject Management Endpoints ==========

    @Operation(
            summary = "Add subject to exam class",
            description = "Adds a subject to an exam class. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/classes/{examClassUuid}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamSubjectResponse>> addSubjectToExamClass(
            @Parameter(description = "UUID of the exam class")
            @PathVariable String examClassUuid,
            @Valid @RequestBody AddSubjectToExamClassRequest request
    ) {
        ExamSubjectResponse examSubject = examService.addSubjectToExamClass(examClassUuid, request);
        return ResponseEntity.ok(ApiResponse.success(examSubject, "Subject added to exam class successfully"));
    }

    @Operation(
            summary = "Update exam subject",
            description = "Updates exam subject details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PutMapping("/subjects/{examSubjectUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamSubjectResponse>> updateExamSubject(
            @Parameter(description = "UUID of the exam subject to update")
            @PathVariable String examSubjectUuid,
            @Valid @RequestBody AddSubjectToExamClassRequest request
    ) {
        ExamSubjectResponse examSubject = examService.updateExamSubject(examSubjectUuid, request);
        return ResponseEntity.ok(ApiResponse.success(examSubject, "Exam subject updated successfully"));
    }

    @Operation(
            summary = "Remove subject from exam class",
            description = "Removes a subject from an exam class. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @DeleteMapping("/subjects/{examSubjectUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> removeSubjectFromExamClass(
            @Parameter(description = "UUID of the exam subject to remove")
            @PathVariable String examSubjectUuid
    ) {
        examService.removeSubjectFromExamClass(examSubjectUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject removed from exam class successfully"));
    }

    @Operation(
            summary = "Get exam subject by UUID",
            description = "Retrieves exam subject information. Accessible by all authenticated users."
    )
    @GetMapping("/subjects/{examSubjectUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExamSubjectResponse>> getExamSubject(
            @Parameter(description = "UUID of the exam subject")
            @PathVariable String examSubjectUuid
    ) {
        ExamSubjectResponse examSubject = examService.getExamSubjectByUuid(examSubjectUuid);
        return ResponseEntity.ok(ApiResponse.success(examSubject, "Exam subject retrieved successfully"));
    }

    @Operation(
            summary = "Get exam subjects by exam class",
            description = "Retrieves all subjects for an exam class. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{examClassUuid}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ExamSubjectResponse>>> getExamSubjectsByExamClass(
            @Parameter(description = "UUID of the exam class")
            @PathVariable String examClassUuid
    ) {
        List<ExamSubjectResponse> subjects = examService.getExamSubjectsByExamClass(examClassUuid);
        return ResponseEntity.ok(ApiResponse.success(subjects, "Exam subjects retrieved successfully"));
    }

    @Operation(
            summary = "Assign teacher to exam subject",
            description = "Assigns or reassigns a teacher to evaluate/mark an exam subject. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/subjects/{examSubjectUuid}/assign-teacher")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamSubjectResponse>> assignTeacherToExamSubject(
            @Parameter(description = "UUID of the exam subject")
            @PathVariable String examSubjectUuid,
            @Valid @RequestBody AssignTeacherToExamSubjectRequest request
    ) {
        ExamSubjectResponse examSubject = examService.assignTeacherToExamSubject(examSubjectUuid, request);
        return ResponseEntity.ok(ApiResponse.success(examSubject, "Teacher assigned to exam subject successfully"));
    }

    // ========== Student Marks Management Endpoints ==========

    @Operation(
            summary = "Create student marks",
            description = "Creates marks for a student in an exam subject. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/marks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentMarksResponse>> createStudentMarks(
            @Valid @RequestBody CreateStudentMarksRequest request
    ) {
        StudentMarksResponse marks = examService.createStudentMarks(request);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks created successfully"));
    }

    @Operation(
            summary = "Update student marks",
            description = "Updates marks for a student. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PutMapping("/marks/{studentMarksUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentMarksResponse>> updateStudentMarks(
            @Parameter(description = "UUID of the student marks to update")
            @PathVariable String studentMarksUuid,
            @Valid @RequestBody UpdateStudentMarksRequest request
    ) {
        StudentMarksResponse marks = examService.updateStudentMarks(studentMarksUuid, request);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks updated successfully"));
    }

    @Operation(
            summary = "Bulk update student marks",
            description = "Creates or updates marks for multiple students in an exam subject. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/marks/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentMarksResponse>>> bulkUpdateStudentMarks(
            @Valid @RequestBody BulkStudentMarksRequest request
    ) {
        List<StudentMarksResponse> marks = examService.bulkUpdateStudentMarks(request);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks updated successfully"));
    }

    @Operation(
            summary = "Get student marks by UUID",
            description = "Retrieves student marks by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/marks/{studentMarksUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentMarksResponse>> getStudentMarks(
            @Parameter(description = "UUID of the student marks")
            @PathVariable String studentMarksUuid
    ) {
        StudentMarksResponse marks = examService.getStudentMarksByUuid(studentMarksUuid);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks retrieved successfully"));
    }

    @Operation(
            summary = "Get student marks by exam subject",
            description = "Retrieves all marks for an exam subject. Accessible by all authenticated users."
    )
    @GetMapping("/subjects/{examSubjectUuid}/marks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<StudentMarksResponse>>> getStudentMarksByExamSubject(
            @Parameter(description = "UUID of the exam subject")
            @PathVariable String examSubjectUuid
    ) {
        List<StudentMarksResponse> marks = examService.getStudentMarksByExamSubject(examSubjectUuid);
        return ResponseEntity.ok(ApiResponse.success(marks, "Student marks retrieved successfully"));
    }

    @Operation(
            summary = "Get student exam result",
            description = "Retrieves complete exam result for a student. Accessible by all authenticated users."
    )
    @GetMapping("/{examUuid}/students/{studentUuid}/result")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentExamResultResponse>> getStudentExamResult(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid,
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        StudentExamResultResponse result = examService.getStudentExamResult(studentUuid, examUuid);
        return ResponseEntity.ok(ApiResponse.success(result, "Student exam result retrieved successfully"));
    }

    @Operation(
            summary = "Delete student marks",
            description = "Deletes student marks. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @DeleteMapping("/marks/{studentMarksUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteStudentMarks(
            @Parameter(description = "UUID of the student marks to delete")
            @PathVariable String studentMarksUuid
    ) {
        examService.deleteStudentMarks(studentMarksUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Student marks deleted successfully"));
    }

    // ========== Grade Scale Management Endpoints ==========

    @Operation(
            summary = "Create grade scale",
            description = "Creates a grade scale. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/grade-scales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> createGradeScale(
            @Valid @RequestBody CreateGradeScaleRequest request
    ) {
        GradeScaleResponse gradeScale = examService.createGradeScale(request);
        return ResponseEntity.ok(ApiResponse.success(gradeScale, "Grade scale created successfully"));
    }

    @Operation(
            summary = "Update grade scale",
            description = "Updates grade scale details. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/grade-scales/{gradeScaleUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> updateGradeScale(
            @Parameter(description = "UUID of the grade scale to update")
            @PathVariable String gradeScaleUuid,
            @Valid @RequestBody UpdateGradeScaleRequest request
    ) {
        GradeScaleResponse gradeScale = examService.updateGradeScale(gradeScaleUuid, request);
        return ResponseEntity.ok(ApiResponse.success(gradeScale, "Grade scale updated successfully"));
    }

    @Operation(
            summary = "Get grade scale by UUID",
            description = "Retrieves grade scale information. Accessible by all authenticated users."
    )
    @GetMapping("/grade-scales/{gradeScaleUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<GradeScaleResponse>> getGradeScale(
            @Parameter(description = "UUID of the grade scale")
            @PathVariable String gradeScaleUuid
    ) {
        GradeScaleResponse gradeScale = examService.getGradeScaleByUuid(gradeScaleUuid);
        return ResponseEntity.ok(ApiResponse.success(gradeScale, "Grade scale retrieved successfully"));
    }

    @Operation(
            summary = "Get all grade scales",
            description = "Retrieves a paginated list of all grade scales. Accessible by all authenticated users."
    )
    @GetMapping("/grade-scales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<GradeScaleResponse>>> getAllGradeScales(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "minMarks") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<GradeScaleResponse> gradeScales = examService.getAllGradeScales(pageable);
        return ResponseEntity.ok(ApiResponse.success(gradeScales, "Grade scales retrieved successfully"));
    }

    @Operation(
            summary = "Get all grade scales (without pagination)",
            description = "Retrieves all grade scales as a list. Accessible by all authenticated users."
    )
    @GetMapping("/grade-scales/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<GradeScaleResponse>>> getAllGradeScalesList() {
        List<GradeScaleResponse> gradeScales = examService.getAllGradeScales();
        return ResponseEntity.ok(ApiResponse.success(gradeScales, "Grade scales retrieved successfully"));
    }

    @Operation(
            summary = "Delete grade scale",
            description = "Deletes a grade scale. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/grade-scales/{gradeScaleUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGradeScale(
            @Parameter(description = "UUID of the grade scale to delete")
            @PathVariable String gradeScaleUuid
    ) {
        examService.deleteGradeScale(gradeScaleUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Grade scale deleted successfully"));
    }

    // ========== Student Transcript Management Endpoints ==========

    @Operation(
            summary = "Generate student transcript",
            description = "Generates or regenerates transcript for a student for an academic year. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/transcripts/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentTranscriptResponse>> generateTranscript(
            @Parameter(description = "UUID of the student")
            @RequestParam String studentUuid,
            @Parameter(description = "UUID of the academic year")
            @RequestParam String academicYearUuid
    ) {
        StudentTranscriptResponse transcript = examService.generateTranscript(studentUuid, academicYearUuid);
        return ResponseEntity.ok(ApiResponse.success(transcript, "Transcript generated successfully"));
    }

    @Operation(
            summary = "Update transcript",
            description = "Updates transcript details. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PutMapping("/transcripts/{transcriptUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<StudentTranscriptResponse>> updateTranscript(
            @Parameter(description = "UUID of the transcript to update")
            @PathVariable String transcriptUuid,
            @Valid @RequestBody PublishTranscriptRequest request
    ) {
        StudentTranscriptResponse transcript = examService.updateTranscript(transcriptUuid, request);
        return ResponseEntity.ok(ApiResponse.success(transcript, "Transcript updated successfully"));
    }

    @Operation(
            summary = "Publish transcript",
            description = "Publishes a transcript making it visible to students. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/transcripts/{transcriptUuid}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentTranscriptResponse>> publishTranscript(
            @Parameter(description = "UUID of the transcript to publish")
            @PathVariable String transcriptUuid,
            @Valid @RequestBody PublishTranscriptRequest request
    ) {
        StudentTranscriptResponse transcript = examService.publishTranscript(transcriptUuid, request);
        return ResponseEntity.ok(ApiResponse.success(transcript, "Transcript published successfully"));
    }

    @Operation(
            summary = "Unpublish transcript",
            description = "Unpublishes a transcript. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/transcripts/{transcriptUuid}/unpublish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentTranscriptResponse>> unpublishTranscript(
            @Parameter(description = "UUID of the transcript to unpublish")
            @PathVariable String transcriptUuid
    ) {
        StudentTranscriptResponse transcript = examService.unpublishTranscript(transcriptUuid);
        return ResponseEntity.ok(ApiResponse.success(transcript, "Transcript unpublished successfully"));
    }

    @Operation(
            summary = "Get transcript by UUID",
            description = "Retrieves transcript information. Accessible by all authenticated users."
    )
    @GetMapping("/transcripts/{transcriptUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentTranscriptResponse>> getTranscript(
            @Parameter(description = "UUID of the transcript")
            @PathVariable String transcriptUuid
    ) {
        StudentTranscriptResponse transcript = examService.getTranscriptByUuid(transcriptUuid);
        return ResponseEntity.ok(ApiResponse.success(transcript, "Transcript retrieved successfully"));
    }

    @Operation(
            summary = "Get transcripts by student",
            description = "Retrieves all transcripts for a student. Accessible by all authenticated users."
    )
    @GetMapping("/transcripts/student/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<StudentTranscriptResponse>>> getTranscriptsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        List<StudentTranscriptResponse> transcripts = examService.getTranscriptsByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(transcripts, "Transcripts retrieved successfully"));
    }

    @Operation(
            summary = "Get transcripts by academic year",
            description = "Retrieves all transcripts for an academic year. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/transcripts/academic-year/{academicYearUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentTranscriptResponse>>> getTranscriptsByAcademicYear(
            @Parameter(description = "UUID of the academic year")
            @PathVariable String academicYearUuid,
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
        Page<StudentTranscriptResponse> transcripts = examService.getTranscriptsByAcademicYear(academicYearUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(transcripts, "Transcripts retrieved successfully"));
    }

    @Operation(
            summary = "Get published transcripts by academic year",
            description = "Retrieves published transcripts for an academic year. Accessible by all authenticated users."
    )
    @GetMapping("/transcripts/academic-year/{academicYearUuid}/published")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<StudentTranscriptResponse>>> getPublishedTranscriptsByAcademicYear(
            @Parameter(description = "UUID of the academic year")
            @PathVariable String academicYearUuid,
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
        Page<StudentTranscriptResponse> transcripts = examService.getPublishedTranscriptsByAcademicYear(academicYearUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(transcripts, "Published transcripts retrieved successfully"));
    }

    // ========== Reports and Summaries Endpoints ==========

    @Operation(
            summary = "Get class exam summary",
            description = "Retrieves summary statistics for a class in an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/{examUuid}/classes/{classUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClassExamSummaryResponse>> getClassExamSummary(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid,
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid
    ) {
        ClassExamSummaryResponse summary = examService.getClassExamSummary(examUuid, classUuid);
        return ResponseEntity.ok(ApiResponse.success(summary, "Class exam summary retrieved successfully"));
    }

    @Operation(
            summary = "Get class exam summaries for all classes",
            description = "Retrieves summary statistics for all classes in an exam. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/{examUuid}/classes/summaries")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ClassExamSummaryResponse>>> getClassExamSummaries(
            @Parameter(description = "UUID of the exam")
            @PathVariable String examUuid
    ) {
        List<ClassExamSummaryResponse> summaries = examService.getClassExamSummaries(examUuid);
        return ResponseEntity.ok(ApiResponse.success(summaries, "Class exam summaries retrieved successfully"));
    }
}

