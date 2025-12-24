package org.collegemanagement.entity.finance;

import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoice_tenant", columnList = "college_id"),
                @Index(name = "idx_invoice_subscription", columnList = "subscription_id"),
                @Index(name = "idx_invoice_status", columnList = "status"),
                @Index(name = "idx_invoice_due_date", columnList = "due_date"),
                @Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Invoice extends BaseEntity {

    /**
     * Tenant (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;


    /**
     * Subscription (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    /**
     * Invoice amount (use BigDecimal for money)
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency; // e.g. INR, USD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private Set<Payment> payments;


    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;


    /**
     * Default values
     */
    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = InvoiceStatus.UNPAID;
        }
    }
}
