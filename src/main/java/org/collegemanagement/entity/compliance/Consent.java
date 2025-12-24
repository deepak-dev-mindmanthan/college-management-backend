package org.collegemanagement.entity.compliance;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;

import java.time.Instant;

@Entity
@Table(
        name = "consents",
        indexes = {
                @Index(name = "idx_consent_user", columnList = "user_id"),
                @Index(name = "idx_consent_type", columnList = "consent_type"),
                @Index(name = "idx_consent_time", columnList = "accepted_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Consent extends BaseEntity {

    /**
     * User who gave consent
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Consent type
     * Examples:
     * TERMS_AND_CONDITIONS
     * PRIVACY_POLICY
     * DATA_PROCESSING
     */
    @Column(name = "consent_type", nullable = false, length = 50)
    private String consentType;

    /**
     * When consent was accepted
     */
    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    /**
     * IP address from which consent was given
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Optional user-agent (browser/device)
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (acceptedAt == null) {
            acceptedAt = Instant.now();
        }
    }
}

