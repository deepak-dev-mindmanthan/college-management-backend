package org.collegemanagement.entity.config;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;

@Entity
@Table(
        name = "settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_setting_per_tenant",
                        columnNames = {"college_id", "setting_key"}
                )
        },
        indexes = {
                @Index(name = "idx_setting_college", columnList = "college_id"),
                @Index(name = "idx_setting_key", columnList = "setting_key")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Setting extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Configuration key
     * Example: TIMEZONE, FEE_LATE_DAYS, ATTENDANCE_MODE
     */
    @Column(name = "setting_key", nullable = false, length = 100)
    private String key;

    @Column(length = 300)
    private String description;

    /**
     * Configuration value (stored as string)
     * Parse in service layer (int, boolean, json, etc.)
     */
    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String value;
}

