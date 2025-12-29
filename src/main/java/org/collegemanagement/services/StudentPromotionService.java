package org.collegemanagement.services;

import org.collegemanagement.dto.promotion.PromoteStudentRequest;
import org.collegemanagement.dto.promotion.StudentPromotionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentPromotionService {

    /**
     * Promote a student from one class to another
     * This creates a new enrollment and logs the promotion
     */
    StudentPromotionResponse promoteStudent(PromoteStudentRequest request);

    /**
     * Get promotion log by UUID
     */
    StudentPromotionResponse getPromotionLogByUuid(String promotionLogUuid);

    /**
     * Get all promotion logs with pagination
     */
    Page<StudentPromotionResponse> getAllPromotionLogs(Pageable pageable);

    /**
     * Get promotion history for a student
     */
    List<StudentPromotionResponse> getPromotionHistoryByStudent(String studentUuid);

    /**
     * Get promotion logs by academic year
     */
    Page<StudentPromotionResponse> getPromotionLogsByAcademicYear(String academicYearUuid, Pageable pageable);
}

