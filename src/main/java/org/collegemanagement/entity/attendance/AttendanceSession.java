package org.collegemanagement.entity.attendance;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.AttendanceSessionType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "attendance_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_session",
                        columnNames = {"college_id", "class_id", "date", "session_type"}
                )
        },
        indexes = {
                @Index(name = "idx_att_session_college", columnList = "college_id"),
                @Index(name = "idx_att_session_class", columnList = "class_id"),
                @Index(name = "idx_att_session_date", columnList = "date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AttendanceSession extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Class for which attendance is taken
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    /**
     * Attendance date
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * DAY or PERIOD based attendance
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    private AttendanceSessionType sessionType;

    /**
     * Attendance records for students
     */
    @OneToMany(
            mappedBy = "attendanceSession",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<AttendanceRecord> records = new HashSet<>();
}

