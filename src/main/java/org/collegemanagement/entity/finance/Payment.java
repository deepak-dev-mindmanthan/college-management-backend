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
                @Index(name = "idx_payment_gateway_order", columnList = "gateway_order_id", unique = true),
                @Index(name = "idx_payment_gateway_txn", columnList = "gateway_transaction_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGateway gateway;

    @Column(name = "gateway_order_id", unique = true, length = 100)
    private String gatewayOrderId;

    @Column(name = "gateway_transaction_id", unique = true, length = 100)
    private String gatewayTransactionId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "payment_date")
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


