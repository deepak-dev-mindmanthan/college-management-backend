package org.collegemanagement.entity.admission;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.AdmissionStatus;
import org.collegemanagement.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "admission_applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_application_no",
                        columnNames = {"college_id", "application_no"}
                )
        },
        indexes = {
                @Index(name = "idx_admission_college", columnList = "college_id"),
                @Index(name = "idx_admission_status", columnList = "status"),
                @Index(name = "idx_admission_class", columnList = "applied_class_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdmissionApplication extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * System-generated application number
     */
    @Column(name = "application_no", nullable = false, length = 50)
    private String applicationNo;

    /**
     * Applicant details
     */
    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    /**
     * Class applied for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_class_id")
    private ClassRoom appliedClass;

    @Column(name = "previous_school", length = 200)
    private String previousSchool;

    /**
     * Uploaded documents metadata (JSON)
     */
    @Column(name = "documents_json", columnDefinition = "TEXT")
    private String documentsJson;

    /**
     * Admission lifecycle status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdmissionStatus status;

    /**
     * When application was submitted
     */
    @Column(name = "submitted_at")
    private Instant submittedAt;
}

