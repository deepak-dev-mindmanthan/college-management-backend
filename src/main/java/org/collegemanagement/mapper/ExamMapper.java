package org.collegemanagement.mapper;

import org.collegemanagement.dto.exam.ExamClassResponse;
import org.collegemanagement.dto.exam.ExamResponse;
import org.collegemanagement.entity.exam.Exam;
import org.collegemanagement.entity.exam.ExamClass;
import org.collegemanagement.entity.exam.ExamSubject;

import java.util.Collections;
import java.util.stream.Collectors;

public final class ExamMapper {

    private ExamMapper() {
    }

    /**
     * Convert Exam entity to ExamResponse
     */
    public static ExamResponse toResponse(Exam exam) {
        if (exam == null) {
            return null;
        }

        return ExamResponse.builder()
                .uuid(exam.getUuid())
                .name(exam.getName())
                .examType(exam.getExamType())
                .academicYearUuid(exam.getAcademicYear() != null ? exam.getAcademicYear().getUuid() : null)
                .academicYearName(exam.getAcademicYear() != null ? exam.getAcademicYear().getYearName() : null)
                .startDate(exam.getStartDate())
                .endDate(exam.getEndDate())
                .collegeId(exam.getCollege() != null ? exam.getCollege().getId() : null)
                .examClasses(exam.getExamClasses() != null ?
                        exam.getExamClasses().stream()
                                .map(ExamMapper::toExamClassResponse)
                                .collect(Collectors.toSet()) :
                        Collections.emptySet())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    /**
     * Convert ExamClass entity to ExamClassResponse
     */
    public static ExamClassResponse toExamClassResponse(ExamClass examClass) {
        if (examClass == null) {
            return null;
        }

        return ExamClassResponse.builder()
                .uuid(examClass.getUuid())
                .examUuid(examClass.getExam() != null ? examClass.getExam().getUuid() : null)
                .classUuid(examClass.getClassRoom() != null ? examClass.getClassRoom().getUuid() : null)
                .className(examClass.getClassRoom() != null ? examClass.getClassRoom().getName() : null)
                .section(examClass.getClassRoom() != null ? examClass.getClassRoom().getSection() : null)
                .subjects(examClass.getSubjects() != null ?
                        examClass.getSubjects().stream()
                                .map(ExamMapper::toExamSubjectResponse)
                                .collect(Collectors.toSet()) :
                        Collections.emptySet())
                .createdAt(examClass.getCreatedAt())
                .updatedAt(examClass.getUpdatedAt())
                .build();
    }

    /**
     * Convert ExamSubject entity to ExamSubjectResponse
     */
    public static org.collegemanagement.dto.exam.ExamSubjectResponse toExamSubjectResponse(ExamSubject examSubject) {
        if (examSubject == null) {
            return null;
        }

        return org.collegemanagement.dto.exam.ExamSubjectResponse.builder()
                .uuid(examSubject.getUuid())
                .examClassUuid(examSubject.getExamClass() != null ? examSubject.getExamClass().getUuid() : null)
                .subjectUuid(examSubject.getSubject() != null ? examSubject.getSubject().getUuid() : null)
                .subjectName(examSubject.getSubject() != null ? examSubject.getSubject().getName() : null)
                .subjectCode(examSubject.getSubject() != null ? examSubject.getSubject().getCode() : null)
                .maxMarks(examSubject.getMaxMarks())
                .passMarks(examSubject.getPassMarks())
                .examDate(examSubject.getExamDate())
                .totalStudents(examSubject.getMarks() != null ? examSubject.getMarks().size() : 0)
                .studentsWithMarks(examSubject.getMarks() != null ?
                        (int) examSubject.getMarks().stream()
                                .filter(m -> m != null && m.getMarksObtained() != null)
                                .count() : 0)
                .assignedTeacherUuid(examSubject.getAssignedTeacher() != null ? examSubject.getAssignedTeacher().getUuid() : null)
                .assignedTeacherName(examSubject.getAssignedTeacher() != null ? examSubject.getAssignedTeacher().getName() : null)
                .createdAt(examSubject.getCreatedAt())
                .updatedAt(examSubject.getUpdatedAt())
                .build();
    }
}

