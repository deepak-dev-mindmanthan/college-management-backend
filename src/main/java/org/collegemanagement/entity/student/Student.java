package org.collegemanagement.entity.student;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.academic.StudentPromotionLog;
import org.collegemanagement.entity.attendance.AttendanceRecord;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.discipline.DisciplinaryCase;
import org.collegemanagement.entity.exam.StudentMarks;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.entity.hostel.HostelAllocation;
import org.collegemanagement.entity.ptm.PTMBooking;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.transport.TransportAllocation;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.Gender;
import org.collegemanagement.enums.Status;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_roll_per_college",
                        columnNames = {"college_id", "roll_number"}
                ),
                @UniqueConstraint(
                        name = "uk_student_reg_per_college",
                        columnNames = {"college_id", "registration_number"}
                ),
                @UniqueConstraint(
                        name = "uk_student_user",
                        columnNames = {"user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_student_college", columnList = "college_id"),
                @Index(name = "idx_student_user", columnList = "user_id"),
                @Index(name = "idx_student_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Student extends BaseEntity {

    /**
     * Tenant (College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Login account (User)
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ParentStudent> parents = new HashSet<>();

    @Column(name = "roll_number", nullable = false, length = 50)
    private String rollNumber;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "admission_date", nullable = false)
    private Instant admissionDate;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private Set<AttendanceRecord> attendanceRecords;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private Set<HostelAllocation> hostelAllocations;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StudentEnrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StudentMarks> marks = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StudentFee> studentFees = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<DisciplinaryCase> disciplinaryCases = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StudentPromotionLog> promotionLogs = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TransportAllocation> transportAllocations = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PTMBooking> ptmBookings = new HashSet<>();

    @Column(nullable = false)
    private Instant dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;
}


