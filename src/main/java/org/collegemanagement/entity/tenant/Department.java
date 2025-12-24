package org.collegemanagement.entity.tenant;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_name_per_tenant",
                        columnNames = {"college_id", "name"}
                ),
                @UniqueConstraint(
                        name = "uk_department_code_per_tenant",
                        columnNames = {"college_id", "code"}
                )
        },
        indexes = {
                @Index(name = "idx_department_college", columnList = "college_id"),
                @Index(name = "idx_department_head", columnList = "head_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Department extends BaseEntity {
    /**
     * Tenant (College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Department name (e.g. Computer Science)
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Short code (e.g. CSE, ECE)
     */
    @Column(nullable = false, length = 20)
    private String code;

    /**
     * Head of Department (HOD)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_user_id")
    private User head;

}

