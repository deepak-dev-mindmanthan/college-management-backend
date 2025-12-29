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
import org.collegemanagement.dto.promotion.PromoteStudentRequest;
import org.collegemanagement.dto.promotion.StudentPromotionResponse;
import org.collegemanagement.services.StudentPromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-promotions")
@AllArgsConstructor
@Tag(name = "Student Promotion Management", description = "APIs for managing student promotions")
public class StudentPromotionController {

    private final StudentPromotionService studentPromotionService;

    @Operation(
            summary = "Promote a student",
            description = "Promotes a student from one class to another. Creates a new enrollment and logs the promotion. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student promoted successfully",
                    content = @Content(schema = @Schema(implementation = StudentPromotionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student, class, or academic year not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Student already enrolled for the academic year or no active enrollment found"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentPromotionResponse>> promoteStudent(
            @Valid @RequestBody PromoteStudentRequest request
    ) {
        StudentPromotionResponse promotion = studentPromotionService.promoteStudent(request);
        return ResponseEntity.ok(ApiResponse.success(promotion, "Student promoted successfully"));
    }

    @Operation(
            summary = "Get promotion log by UUID",
            description = "Retrieves a promotion log by UUID. Accessible by appropriate roles."
    )
    @GetMapping("/{promotionLogUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<StudentPromotionResponse>> getPromotionLog(
            @Parameter(description = "UUID of the promotion log")
            @PathVariable String promotionLogUuid
    ) {
        StudentPromotionResponse promotion = studentPromotionService.getPromotionLogByUuid(promotionLogUuid);
        return ResponseEntity.ok(ApiResponse.success(promotion, "Promotion log retrieved successfully"));
    }

    @Operation(
            summary = "Get all promotion logs",
            description = "Retrieves all promotion logs with pagination. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentPromotionResponse>>> getAllPromotionLogs(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StudentPromotionResponse> promotions = studentPromotionService.getAllPromotionLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(promotions, "Promotion logs retrieved successfully"));
    }

    @Operation(
            summary = "Get promotion history for a student",
            description = "Retrieves all promotion logs for a specific student. Accessible by appropriate roles."
    )
    @GetMapping("/student/{studentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<StudentPromotionResponse>>> getPromotionHistoryByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid
    ) {
        List<StudentPromotionResponse> promotions = studentPromotionService.getPromotionHistoryByStudent(studentUuid);
        return ResponseEntity.ok(ApiResponse.success(promotions, "Promotion history retrieved successfully"));
    }

    @Operation(
            summary = "Get promotion logs by academic year",
            description = "Retrieves all promotion logs for a specific academic year. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/academic-year/{academicYearUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentPromotionResponse>>> getPromotionLogsByAcademicYear(
            @Parameter(description = "UUID of the academic year")
            @PathVariable String academicYearUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StudentPromotionResponse> promotions = studentPromotionService.getPromotionLogsByAcademicYear(
                academicYearUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(promotions, "Promotion logs retrieved successfully"));
    }
}

