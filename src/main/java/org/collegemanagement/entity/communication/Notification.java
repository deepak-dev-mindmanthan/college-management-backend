package org.collegemanagement.entity.communication;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.NotificationType;

import org.collegemanagement.enums.NotificationReferenceType;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_read", columnList = "is_read"),
                @Index(name = "idx_notification_type", columnList = "type"),
                @Index(name = "idx_notification_reference", columnList = "reference_type, reference_id"),
                @Index(name = "idx_notification_expires", columnList = "expires_at"),
                @Index(name = "idx_notification_priority", columnList = "priority")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    /**
     * Target user who receives the notification
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Short title (UI / push title / email subject)
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Notification channel
     * IN_APP / EMAIL / SMS / PUSH
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    /**
     * Main notification message
     */
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * Generic reference to domain entity
     * (EXAM, STUDENT_FEE, LEAVE_REQUEST, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private NotificationReferenceType referenceType;

    /**
     * ID of the referenced entity
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Optional deep-link or frontend route
     * Example: /student/fees/123
     */
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    /**
     * Expiry time after which notification should be hidden
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /**
     * Priority (higher = more important)
     * Suggested: 1 = LOW, 5 = MEDIUM, 10 = HIGH
     */
    @NotNull
    @Column(name = "priority", nullable = false)
    @ColumnDefault("5")
    private int priority = 5;

    /**
     * Read / unread flag
     */
    @ColumnDefault("false")
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
}
