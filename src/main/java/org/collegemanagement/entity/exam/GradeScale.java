package org.collegemanagement.entity.exam;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;

import java.math.BigDecimal;

@Entity
@Table(
        name = "grade_scales",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_grade_scale",
                        columnNames = {"college_id", "grade"}
                )
        },
        indexes = {
                @Index(name = "idx_grade_scale_college", columnList = "college_id"),
                @Index(name = "idx_grade_scale_marks", columnList = "min_marks, max_marks")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GradeScale extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 5)
    private String grade;

    @Column(name = "min_marks", nullable = false)
    private Integer minMarks;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    /**
     * Grade points (used in CGPA calculation)
     */
    @Column(name = "grade_points", precision = 3, scale = 2, nullable = false)
    private BigDecimal gradePoints;
}
