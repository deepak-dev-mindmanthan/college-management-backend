package org.collegemanagement.entity.academic;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;

import java.time.Instant;

@Entity
@Table(
        name = "student_promotion_logs",
        indexes = {
                @Index(name = "idx_promotion_college", columnList = "college_id"),
                @Index(name = "idx_promotion_student", columnList = "student_id"),
                @Index(name = "idx_promotion_from_class", columnList = "from_class_id"),
                @Index(name = "idx_promotion_to_class", columnList = "to_class_id"),
                @Index(name = "idx_promotion_year", columnList = "academic_year_id"),
                @Index(name = "idx_promotion_by", columnList = "promoted_by"),
                @Index(name = "idx_promotion_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudentPromotionLog extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Student being promoted
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Class before promotion
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_class_id", nullable = false)
    private ClassRoom fromClass;

    /**
     * Class after promotion
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_class_id", nullable = false)
    private ClassRoom toClass;

    /**
     * Academic year in which promotion happened
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /**
     * Admin / Staff who performed the promotion
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promoted_by", nullable = false)
    private User promotedBy;

    /**
     * Optional promotion reason / remarks
     */
    @Column(length = 500)
    private String remarks;

}

