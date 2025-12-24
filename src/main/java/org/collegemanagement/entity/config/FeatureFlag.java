package org.collegemanagement.entity.config;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;

@Entity
@Table(
        name = "feature_flags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feature_flag_per_tenant",
                        columnNames = {"college_id", "feature_key"}
                )
        },
        indexes = {
                @Index(name = "idx_feature_flag_college", columnList = "college_id"),
                @Index(name = "idx_feature_flag_key", columnList = "feature_key"),
                @Index(name = "idx_feature_flag_enabled", columnList = "is_enabled")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeatureFlag extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Unique feature identifier
     * Example: ENABLE_ONLINE_FEES, EXAM_MODULE_V2
     */
    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(length = 500)
    private String description;



    /**
     * Whether the feature is enabled for this tenant
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled = false;
}

