package org.collegemanagement.entity.discipline;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.DisciplinaryStatus;

import java.time.LocalDate;

@Entity
@Table(
        name = "disciplinary_cases",
        indexes = {
                @Index(name = "idx_discipline_college", columnList = "college_id"),
                @Index(name = "idx_discipline_student", columnList = "student_id"),
                @Index(name = "idx_discipline_status", columnList = "status"),
                @Index(name = "idx_discipline_incident", columnList = "incident_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DisciplinaryCase extends BaseEntity {

    /**
     * Tenant (School / College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Student involved
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * User who reported the incident (Teacher / Staff)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    /**
     * Date of incident
     */
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    /**
     * Incident description
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Action taken by admin
     */
    @Column(name = "action_taken", length = 1000)
    private String actionTaken;

    /**
     * Case lifecycle status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisciplinaryStatus status;
}

