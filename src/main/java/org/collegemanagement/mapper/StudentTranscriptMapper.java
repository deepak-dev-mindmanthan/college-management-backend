package org.collegemanagement.mapper;

import org.collegemanagement.dto.exam.StudentMarksResponse;
import org.collegemanagement.dto.exam.StudentTranscriptResponse;
import org.collegemanagement.entity.exam.StudentMarks;
import org.collegemanagement.entity.exam.StudentTranscript;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StudentTranscriptMapper {

    private StudentTranscriptMapper() {
    }

    /**
     * Convert StudentTranscript entity to StudentTranscriptResponse
     */
    public static StudentTranscriptResponse toResponse(StudentTranscript transcript) {
        return toResponse(transcript, null);
    }

    /**
     * Convert StudentTranscript entity to StudentTranscriptResponse with marks
     */
    public static StudentTranscriptResponse toResponse(StudentTranscript transcript, List<StudentMarks> marks) {
        if (transcript == null) {
            return null;
        }

        List<StudentMarksResponse> marksResponse = marks != null ?
                marks.stream()
                        .map(StudentMarksMapper::toResponse)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return StudentTranscriptResponse.builder()
                .uuid(transcript.getUuid())
                .studentUuid(transcript.getStudent() != null ? transcript.getStudent().getUuid() : null)
                .studentName(transcript.getStudent() != null &&
                        transcript.getStudent().getUser() != null ?
                        transcript.getStudent().getUser().getName() : null)
                .rollNumber(transcript.getStudent() != null ? transcript.getStudent().getRollNumber() : null)
                .academicYearUuid(transcript.getAcademicYear() != null ? transcript.getAcademicYear().getUuid() : null)
                .academicYearName(transcript.getAcademicYear() != null ? transcript.getAcademicYear().getYearName() : null)
                .cgpa(transcript.getCgpa())
                .totalCredits(transcript.getTotalCredits())
                .resultStatus(transcript.getResultStatus())
                .published(transcript.getPublished())
                .approvedByUuid(transcript.getApprovedBy() != null ? transcript.getApprovedBy().getUuid() : null)
                .approvedByName(transcript.getApprovedBy() != null ? transcript.getApprovedBy().getName() : null)
                .publishedAt(transcript.getPublishedAt())
                .remarks(transcript.getRemarks())
                .marks(marksResponse)
                .createdAt(transcript.getCreatedAt())
                .updatedAt(transcript.getUpdatedAt())
                .build();
    }
}

