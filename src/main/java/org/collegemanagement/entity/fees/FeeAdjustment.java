package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.AdjustmentType;

import java.math.BigDecimal;

@Entity
@Table(
        name = "fee_adjustments",
        indexes = {
                @Index(name = "idx_fee_adjustment_student_fee", columnList = "student_fee_id"),
                @Index(name = "idx_fee_adjustment_type", columnList = "type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeeAdjustment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fee_id", nullable = false)
    private StudentFee studentFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdjustmentType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String reason;
}
