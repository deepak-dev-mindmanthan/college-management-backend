package org.collegemanagement.entity.fees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "fee_structures",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fee_structure_per_class",
                        columnNames = {"college_id", "class_id"}
                )
        },
        indexes = {
                @Index(name = "idx_fee_structure_college", columnList = "college_id"),
                @Index(name = "idx_fee_structure_class", columnList = "class_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeeStructure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "due_date")
    private java.time.LocalDate dueDate;

    @OneToMany(
            mappedBy = "feeStructure",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FeeComponent> components = new HashSet<>();

    @OneToMany(
            mappedBy = "feeStructure",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<FeeInstallmentTemplate> installmentTemplates = new HashSet<>();
}

