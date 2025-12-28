package org.collegemanagement.mapper;

import org.collegemanagement.dto.exam.StudentMarksResponse;
import org.collegemanagement.entity.exam.StudentMarks;

public final class StudentMarksMapper {

    private StudentMarksMapper() {
    }

    /**
     * Convert StudentMarks entity to StudentMarksResponse
     */
    public static StudentMarksResponse toResponse(StudentMarks studentMarks) {
        if (studentMarks == null) {
            return null;
        }

        Integer maxMarks = studentMarks.getExamSubject() != null ? studentMarks.getExamSubject().getMaxMarks() : null;
        Integer passMarks = studentMarks.getExamSubject() != null ? studentMarks.getExamSubject().getPassMarks() : null;
        Integer marksObtained = studentMarks.getMarksObtained();
        Boolean isPassed = (marksObtained != null && passMarks != null) ? marksObtained >= passMarks : null;

        return StudentMarksResponse.builder()
                .uuid(studentMarks.getUuid())
                .examSubjectUuid(studentMarks.getExamSubject() != null ? studentMarks.getExamSubject().getUuid() : null)
                .examSubjectName(studentMarks.getExamSubject() != null &&
                        studentMarks.getExamSubject().getSubject() != null ?
                        studentMarks.getExamSubject().getSubject().getName() : null)
                .studentUuid(studentMarks.getStudent() != null ? studentMarks.getStudent().getUuid() : null)
                .studentName(studentMarks.getStudent() != null &&
                        studentMarks.getStudent().getUser() != null ?
                        studentMarks.getStudent().getUser().getName() : null)
                .rollNumber(studentMarks.getStudent() != null ? studentMarks.getStudent().getRollNumber() : null)
                .marksObtained(marksObtained)
                .maxMarks(maxMarks)
                .passMarks(passMarks)
                .grade(studentMarks.getGradeScale() != null ? studentMarks.getGradeScale().getGrade() : null)
                .gradePoints(studentMarks.getGradeScale() != null ? studentMarks.getGradeScale().getGradePoints() : null)
                .isPassed(isPassed)
                .createdAt(studentMarks.getCreatedAt())
                .updatedAt(studentMarks.getUpdatedAt())
                .build();
    }
}

