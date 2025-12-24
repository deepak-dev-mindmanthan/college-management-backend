package org.collegemanagement.entity.student;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.RelationType;

@Entity
@Table(
        name = "parent_students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parent_student",
                        columnNames = {"parent_id", "student_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ps_parent", columnList = "parent_id"),
                @Index(name = "idx_ps_student", columnList = "student_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ParentStudent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RelationType relation;
}

