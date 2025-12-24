package org.collegemanagement.entity.attendance;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.AttendanceStatus;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_student_per_session",
                        columnNames = {"attendance_session_id", "student_id"}
                )
        },
        indexes = {
                @Index(name = "idx_att_record_session", columnList = "attendance_session_id"),
                @Index(name = "idx_att_record_student", columnList = "student_id"),
                @Index(name = "idx_att_record_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AttendanceRecord extends BaseEntity {

    /**
     * Attendance session (DAY / PERIOD)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    /**
     * Student whose attendance is recorded
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * PRESENT / ABSENT / LATE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;
}

