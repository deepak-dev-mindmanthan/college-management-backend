package org.collegemanagement.entity.communication;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.TargetRole;

import java.time.Instant;

@Entity
@Table(
        name = "announcements",
        indexes = {
                @Index(name = "idx_announcement_college", columnList = "college_id"),
                @Index(name = "idx_announcement_role", columnList = "target_role"),
                @Index(name = "idx_announcement_published", columnList = "published_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Announcement extends BaseEntity {

    /**
     * Tenant (College)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    /**
     * Target role audience
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 30)
    private TargetRole targetRole;

    /**
     * When announcement goes live
     */
    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
}

