package org.collegemanagement.entity.exam;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.ResultStatus;

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(
        name = "student_transcripts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_transcript",
                        columnNames = {"student_id", "academic_year_id"}
                )
        },
        indexes = {
                @Index(name = "idx_transcript_student", columnList = "student_id"),
                @Index(name = "idx_transcript_year", columnList = "academic_year_id"),
                @Index(name = "idx_transcript_published", columnList = "published"),
                @Index(name = "idx_transcript_result", columnList = "result_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudentTranscript extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /**
     * Final CGPA / GPA (exact value)
     */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false, length = 20)
    private ResultStatus resultStatus;

    @Column(nullable = false)
    private Boolean published = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(length = 500)
    private String remarks;
}
