package org.collegemanagement.entity.academic;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;


@Entity
@Table(
        name = "class_subject_teachers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_class_subject_teacher",
                        columnNames = {"class_id", "subject_id", "teacher_id"}
                )
        },
        indexes = {
                @Index(name = "idx_cst_class", columnList = "class_id"),
                @Index(name = "idx_cst_subject", columnList = "subject_id"),
                @Index(name = "idx_cst_teacher", columnList = "teacher_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClassSubjectTeacher extends BaseEntity {

    /**
     * Class (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    /**
     * Subject (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Teacher (User with TEACHER role)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

}

