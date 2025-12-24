package org.collegemanagement.entity.compliance;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;

@Entity
@Table(
        name = "data_retention_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_retention_policy",
                        columnNames = {"college_id", "entity_name"}
                )
        },
        indexes = {
                @Index(name = "idx_retention_college", columnList = "college_id"),
                @Index(name = "idx_retention_entity", columnList = "entity_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DataRetentionPolicy extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Entity / module name
     * Examples:
     * STUDENT_MARKS, AUDIT_LOGS, LOGIN_LOGS
     */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /**
     * Retention duration in days
     */
    @Column(name = "retention_period_days", nullable = false)
    private Integer retentionPeriodDays;

    /**
     * Whether policy is active
     */
    @Column(nullable = false)
    private Boolean active = true;
}

