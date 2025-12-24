package org.collegemanagement.entity.academic;


import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.EnrollmentStatus;


@Entity
@Table(
        name = "student_enrollments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_year",
                        columnNames = {"student_id", "academic_year_id"}
                )
        },
        indexes = {
                @Index(name = "idx_enrollment_college", columnList = "college_id"),
                @Index(name = "idx_enrollment_student", columnList = "student_id"),
                @Index(name = "idx_enrollment_year", columnList = "academic_year_id"),
                @Index(name = "idx_enrollment_class", columnList = "class_id"),
                @Index(name = "idx_enrollment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudentEnrollment extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Student identity
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Academic year
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /**
     * Class assigned for this year
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    /**
     * Roll number (year-specific)
     */
    @Column(name = "roll_number", length = 50)
    private String rollNumber;

    /**
     * Enrollment lifecycle status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;
}

