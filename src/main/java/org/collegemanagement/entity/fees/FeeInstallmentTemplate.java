package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "fee_installment_templates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fee_installment_template_name",
                        columnNames = {"fee_structure_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_fee_installment_template_structure", columnList = "fee_structure_id"),
                @Index(name = "idx_fee_installment_template_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeeInstallmentTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
}
