package org.collegemanagement.entity.finance;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.ExpenseStatus;

import java.time.Instant;

@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expense_college", columnList = "college_id"),
                @Index(name = "idx_expense_category", columnList = "category_id"),
                @Index(name = "idx_expense_date", columnList = "expense_date"),
                @Index(name = "idx_expense_status", columnList = "status"),
                @Index(name = "idx_expense_created_by", columnList = "created_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Expense extends BaseEntity {

    /* =========================
       TENANT
       ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /* =========================
       CATEGORY
       ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    /* =========================
       MONEY
       ========================= */

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /* =========================
       DESCRIPTION
       ========================= */

    @Column(length = 1000)
    private String description;

    /* =========================
       DATES
       ========================= */

    /**
     * Actual date of expense (bill date)
     */
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    /* =========================
       WORKFLOW
       ========================= */

    /**
     * DRAFT → SUBMITTED → APPROVED → PAID / REJECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpenseStatus status;

    /**
     * User who created the expense
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * User who approved the expense
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /**
     * Approval timestamp
     */
    @Column(name = "approved_at")
    private Instant approvedAt;

    /* =========================
       PAYMENT INFO (OPTIONAL)
       ========================= */

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "paid_at")
    private Instant paidAt;

    /* =========================
       LIFECYCLE
       ========================= */

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ExpenseStatus.DRAFT;
        }
    }
}
