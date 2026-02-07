package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.PaymentMode;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "fee_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fee_payment_transaction",
                        columnNames = {"transaction_id"}
                )
        },
        indexes = {
                @Index(name = "idx_fee_payment_student_fee", columnList = "student_fee_id"),
                @Index(name = "idx_fee_payment_txn", columnList = "transaction_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fee_id", nullable = false)
    private StudentFee studentFee;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 30)
    private PaymentMode paymentMode;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;

    @OneToOne(mappedBy = "feePayment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private FeeReceipt receipt;
}

