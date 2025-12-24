package org.collegemanagement.entity.academic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.CourseType;


@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_per_college",
                        columnNames = {"college_id", "code"}
                )
        },
        indexes = {
                @Index(name = "idx_course_college", columnList = "college_id"),
                @Index(name = "idx_course_type", columnList = "course_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Stream / Program name
     * School: Science
     * College: B.Tech Computer Science
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Short code
     * SCI, COM, BTECH-CSE
     */
    @Column(nullable = false, length = 50)
    private String code;

    /**
     * SCHOOL_STREAM / COLLEGE_PROGRAM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 30)
    private CourseType courseType;

    /**
     * Duration in years (nullable for school)
     */
    @Column(name = "duration_years")
    private Integer durationYears;
}

