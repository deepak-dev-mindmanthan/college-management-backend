package org.collegemanagement.entity.staff;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "staff_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_staff_user",
                        columnNames = {"user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_staff_college", columnList = "college_id"),
                @Index(name = "idx_staff_user", columnList = "user_id"),
                @Index(name = "idx_staff_designation", columnList = "designation")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StaffProfile extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Login account
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Designation (Teacher, Clerk, Principal, etc.)
     */
    @Column(nullable = false, length = 100)
    private String designation;

    /**
     * Monthly salary (HR / Payroll)
     */
    @Column(nullable = false)
    private BigDecimal salary;

    /**
     * Joining date
     */
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;
}

