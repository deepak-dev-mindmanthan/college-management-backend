package org.collegemanagement.entity.leave;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;

import java.time.LocalDate;

@Entity
@Table(
        name = "leave_requests",
        indexes = {
                @Index(name = "idx_leave_user", columnList = "user_id"),
                @Index(name = "idx_leave_status", columnList = "status"),
                @Index(name = "idx_leave_dates", columnList = "start_date, end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LeaveRequest extends BaseEntity {

    /**
     * User requesting leave (Staff / Student)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveOwnerType ownerType;

    /**
     * Type of leave (CASUAL, SICK, ANNUAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;


    /**
     * Leave start date
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Leave end date
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Leave status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status;

    /**
     * Approver (Admin / Principal / Manager)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /**
     * Optional reason provided by user
     */
    @Column(length = 500)
    private String reason;

    /**
     * Admin / approver remarks
     */
    @Column(name = "approver_comment", length = 500)
    private String approverComment;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = LeaveStatus.PENDING;
        }
    }
}

