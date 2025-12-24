package org.collegemanagement.entity.academic;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.timetable.Timetable;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "classes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_class_per_year_college",
                        columnNames = {"college_id", "academic_year_id", "name", "section"}
                )
        },
        indexes = {
                @Index(name = "idx_class_tenant", columnList = "college_id"),
                @Index(name = "idx_class_academic_year", columnList = "academic_year_id"),
                @Index(name = "idx_class_teacher", columnList = "class_teacher_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClassRoom extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Academic Year (e.g. 2024–25)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /**
     * Class name (e.g. 10, BSc, MCA)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Section (e.g. A, B, Blue)
     */
    @Column(length = 20)
    private String section;

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private Set<Subject> subjects = new HashSet<>();

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private Set<ClassSubjectTeacher> subjectTeachers;

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private Set<Timetable> timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id") // nullable for class 1-12th
    private Course course;



    /**
     * Class Teacher / Tutor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher;

}
