package org.collegemanagement.entity.exam;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.ExamType;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "exams",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_name_per_year",
                        columnNames = {"college_id", "academic_year_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_exam_college", columnList = "college_id"),
                @Index(name = "idx_exam_year", columnList = "academic_year_id"),
                @Index(name = "idx_exam_type", columnList = "exam_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Exam extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Exam name
     * Examples:
     * - Mid Term Exam
     * - Final Exam
     * - Unit Test 1
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * MIDTERM / FINAL / UNIT_TEST / INTERNAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 30)
    private ExamType examType;

    /**
     * Academic year in which exam is conducted
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /**
     * Exam start date (optional but useful)
     */
    @Column(name = "start_date")
    private Instant startDate;

    /**
     * Exam end date
     */
    @Column(name = "end_date")
    private Instant endDate;

    /**
     * Classes participating in this exam
     */
    @OneToMany(
            mappedBy = "exam",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ExamClass> examClasses = new HashSet<>();
}


