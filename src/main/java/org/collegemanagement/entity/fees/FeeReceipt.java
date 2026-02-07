package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.user.User;

import java.time.Instant;

@Entity
@Table(
        name = "fee_receipts",
        indexes = {
                @Index(name = "idx_fee_receipt_number", columnList = "receipt_number", unique = true),
                @Index(name = "idx_fee_receipt_payment", columnList = "fee_payment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeeReceipt extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_payment_id", nullable = false, unique = true)
    private FeePayment feePayment;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id")
    private User issuedBy;
}
