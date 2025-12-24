package org.collegemanagement.entity.academic;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;


import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.timetable.Timetable;
import org.collegemanagement.entity.base.BaseEntity;

import java.util.Set;

@Entity
@Table(
        name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subject_code_per_class",
                        columnNames = { "class_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uk_subject_name_per_class",
                        columnNames = { "class_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_subject_class", columnList = "class_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Subject extends BaseEntity {

    /**
     * Class in which this subject is taught
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private Set<ClassSubjectTeacher> classTeachers;

    /**
     * Subject name (e.g. Mathematics)
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Subject code (e.g. MATH101)
     */
    @Column(nullable = false, length = 50)
    private String code;


    /**
     * Timetable slots
     */
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private Set<Timetable> timetableSlots;


    /**
     * Credit value (mostly for colleges)
     */
    @Column(nullable = false)
    @Min(1)
    private Integer credit;

}
