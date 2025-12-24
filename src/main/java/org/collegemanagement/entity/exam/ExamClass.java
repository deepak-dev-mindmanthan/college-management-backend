package org.collegemanagement.entity.exam;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "exam_classes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_class",
                        columnNames = {"exam_id", "class_id"}
                )
        },
        indexes = {
                @Index(name = "idx_exam_class_exam", columnList = "exam_id"),
                @Index(name = "idx_exam_class_class", columnList = "class_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExamClass extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @OneToMany(mappedBy = "examClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ExamSubject> subjects = new HashSet<>();
}

