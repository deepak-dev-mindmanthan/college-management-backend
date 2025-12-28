package org.collegemanagement.entity.exam;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "exam_subjects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_subject",
                        columnNames = {"exam_class_id", "subject_id"}
                )
        },
        indexes = {
                @Index(name = "idx_exam_subject_exam_class", columnList = "exam_class_id"),
                @Index(name = "idx_exam_subject_subject", columnList = "subject_id"),
                @Index(name = "idx_exam_subject_date", columnList = "exam_date"),
                @Index(name = "idx_exam_subject_teacher", columnList = "assigned_teacher_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExamSubject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_class_id", nullable = false)
    private ExamClass examClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "pass_marks", nullable = false)
    private Integer passMarks;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    /**
     * Teacher assigned to evaluate/mark this exam subject (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id")
    private User assignedTeacher;

    @OneToMany(mappedBy = "examSubject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<StudentMarks> marks = new HashSet<>();
}

