package org.collegemanagement.entity.finance;

import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
                @Index(name = "idx_payment_gateway", columnList = "gateway"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_txn", columnList = "transaction_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {
    /**
     * Invoice (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGateway gateway;

    /**
     * Gateway transaction reference
     */
    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    /**
     * Defaults
     */
    @PrePersist
    protected void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = Instant.now();
        }
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }
}

