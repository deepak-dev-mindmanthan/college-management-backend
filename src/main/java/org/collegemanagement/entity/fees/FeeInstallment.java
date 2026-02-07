package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "fee_installments",
        indexes = {
                @Index(name = "idx_fee_installment_student_fee", columnList = "student_fee_id"),
                @Index(name = "idx_fee_installment_due_date", columnList = "due_date"),
                @Index(name = "idx_fee_installment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeeInstallment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fee_id", nullable = false)
    private StudentFee studentFee;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_amount", nullable = false)
    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstallmentStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
}
