package org.collegemanagement.mapper;

import org.collegemanagement.dto.exam.GradeScaleResponse;
import org.collegemanagement.entity.exam.GradeScale;

public final class GradeScaleMapper {

    private GradeScaleMapper() {
    }

    /**
     * Convert GradeScale entity to GradeScaleResponse
     */
    public static GradeScaleResponse toResponse(GradeScale gradeScale) {
        if (gradeScale == null) {
            return null;
        }

        return GradeScaleResponse.builder()
                .uuid(gradeScale.getUuid())
                .grade(gradeScale.getGrade())
                .minMarks(gradeScale.getMinMarks())
                .maxMarks(gradeScale.getMaxMarks())
                .gradePoints(gradeScale.getGradePoints())
                .collegeId(gradeScale.getCollege() != null ? gradeScale.getCollege().getId() : null)
                .createdAt(gradeScale.getCreatedAt())
                .updatedAt(gradeScale.getUpdatedAt())
                .build();
    }
}

