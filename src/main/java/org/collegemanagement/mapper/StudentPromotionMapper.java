package org.collegemanagement.mapper;

import org.collegemanagement.dto.promotion.StudentPromotionResponse;
import org.collegemanagement.entity.academic.StudentPromotionLog;

public class StudentPromotionMapper {

    /**
     * Convert StudentPromotionLog entity to StudentPromotionResponse DTO
     */
    public static StudentPromotionResponse toResponse(StudentPromotionLog promotionLog) {
        if (promotionLog == null) {
            return null;
        }

        return StudentPromotionResponse.builder()
                .uuid(promotionLog.getUuid())
                .studentUuid(promotionLog.getStudent() != null ? promotionLog.getStudent().getUuid() : null)
                .studentName(promotionLog.getStudent() != null && promotionLog.getStudent().getUser() != null
                        ? promotionLog.getStudent().getUser().getName() : null)
                .fromClassUuid(promotionLog.getFromClass() != null ? promotionLog.getFromClass().getUuid() : null)
                .fromClassName(promotionLog.getFromClass() != null ? promotionLog.getFromClass().getName() : null)
                .toClassUuid(promotionLog.getToClass() != null ? promotionLog.getToClass().getUuid() : null)
                .toClassName(promotionLog.getToClass() != null ? promotionLog.getToClass().getName() : null)
                .academicYearUuid(promotionLog.getAcademicYear() != null ? promotionLog.getAcademicYear().getUuid() : null)
                .academicYearName(promotionLog.getAcademicYear() != null ? promotionLog.getAcademicYear().getYearName() : null)
                .promotedByUserUuid(promotionLog.getPromotedBy() != null ? promotionLog.getPromotedBy().getUuid() : null)
                .promotedByName(promotionLog.getPromotedBy() != null ? promotionLog.getPromotedBy().getName() : null)
                .remarks(promotionLog.getRemarks())
                .collegeId(promotionLog.getCollege() != null ? promotionLog.getCollege().getId() : null)
                .build();
    }
}

