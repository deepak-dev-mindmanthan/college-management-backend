package org.collegemanagement.entity.exam;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.base.BaseEntity;

@Entity
@Table(
        name = "student_marks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_marks",
                        columnNames = {"exam_subject_id", "student_id"}
                )
        },
        indexes = {
                @Index(name = "idx_marks_exam_subject", columnList = "exam_subject_id"),
                @Index(name = "idx_marks_student", columnList = "student_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudentMarks extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_subject_id", nullable = false)
    private ExamSubject examSubject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "marks_obtained", nullable = false)
    private Integer marksObtained;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_scale_id")
    private GradeScale gradeScale;

}

