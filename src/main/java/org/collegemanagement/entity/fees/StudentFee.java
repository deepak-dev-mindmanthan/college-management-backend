package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.FeeStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "student_fees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_fee",
                        columnNames = {"student_id", "fee_structure_id"}
                )
        },
        indexes = {
                @Index(name = "idx_student_fee_student", columnList = "student_id"),
                @Index(name = "idx_student_fee_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudentFee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_amount", nullable = false)
    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeStatus status;

    @OneToMany(
            mappedBy = "studentFee",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private Set<FeePayment> payments = new HashSet<>();
}

