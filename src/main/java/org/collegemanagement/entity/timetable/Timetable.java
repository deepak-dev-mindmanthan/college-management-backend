package org.collegemanagement.entity.timetable;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.DayOfWeek;


@Entity
@Table(
        name = "timetables",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timetable_slot",
                        columnNames = {
                                "college_id",
                                "class_id",
                                "day_of_week",
                                "period_number"
                        }
                )
        },
        indexes = {
                @Index(name = "idx_timetable_college", columnList = "college_id"),
                @Index(name = "idx_timetable_class", columnList = "class_id"),
                @Index(name = "idx_timetable_subject", columnList = "subject_id"),
                @Index(name = "idx_timetable_teacher", columnList = "teacher_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Timetable extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Class for which timetable is defined
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    /**
     * Day of week
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    /**
     * Period number (1,2,3…)
     */
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    /**
     * Subject taught in this period
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Teacher handling the period
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
}

